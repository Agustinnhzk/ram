package com.ram.reparaciones.adaptadores.persistencia;

import com.ram.TestcontainersConfiguration;
import com.ram.clientes.NuevoCliente;
import com.ram.reparaciones.DatosMovimiento;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.NuevoIngreso;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.VistaDeOrdenes;
import com.ram.tenants.TallerId;
import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Contra PostgreSQL real: aislamiento entre talleres (ADR-005) y que la bitácora se guarde completa y en orden. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AislamientoReparacionesTests {

    @Autowired
    private Reparaciones reparaciones;

    @Autowired
    private JdbcTemplate jdbc;

    private TallerId tallerA;
    private TallerId tallerB;

    @BeforeEach
    void preparar() {
        tallerA = crearTaller("a");
        tallerB = crearTaller("b");
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cadaTallerTieneSuPropiaNumeracionYNoVeLasOrdenesDelOtro() {
        OrdenId deA = ingresar(tallerA, 900);
        OrdenId deB = ingresar(tallerB, 900); // el mismo número en otro taller está bien

        assertThat(ContextoTaller.con(tallerA, () -> reparaciones.listar(VistaDeOrdenes.TODAS, null))).hasSize(1);
        assertThat(ContextoTaller.con(tallerB, () -> reparaciones.obtener(deA))).isEmpty();
        assertThat(ContextoTaller.con(tallerA, () -> reparaciones.obtener(deB))).isEmpty();
        assertThat(ContextoTaller.con(tallerB, () -> reparaciones.seguimiento(900, "1234")))
                .get().satisfies(s -> assertThat(s.equipo()).isEqualTo("Notebook de " + tallerB));
    }

    @Test
    void laBitacoraSeGuardaCompletaYEnOrden() {
        OrdenId id = ingresar(tallerA, 950);
        ContextoTaller.con(tallerA, () -> {
            reparaciones.cambiarEstado(id, EstadoReparacion.EN_DIAGNOSTICO, "Nota interna", false);
            reparaciones.presupuestar(id, 80_000, "Pantalla");
            reparaciones.cambiarEstado(id, EstadoReparacion.APROBADO, null, false);
            return null;
        });

        List<DatosMovimiento> bitacora = ContextoTaller.con(tallerA, () -> reparaciones.obtener(id)).orElseThrow().movimientos();

        assertThat(bitacora).extracting(DatosMovimiento::estado).containsExactly(
                EstadoReparacion.INGRESADO, EstadoReparacion.EN_DIAGNOSTICO, null, EstadoReparacion.PRESUPUESTADO,
                EstadoReparacion.APROBADO);
        Integer delTallerA = jdbc.queryForObject("select count(*) from movimientos_reparacion m join ordenes_reparacion o "
                + "on o.id = m.orden_id where o.id = ? and m.taller_id = ?", Integer.class, id.valor(), tallerA.valor());
        assertThat(delTallerA).isEqualTo(5);
    }

    private OrdenId ingresar(TallerId taller, int numero) {
        loguearDuenoDe(taller);
        return ContextoTaller.con(taller, () -> reparaciones.registrarIngreso(new NuevoIngreso(numero, null,
                new NuevoCliente("Cliente de " + taller, "3415551234", null), "Notebook de " + taller, true, null,
                "clave", "No prende", null)));
    }

    private static void loguearDuenoDe(TallerId taller) {
        UsuarioAutenticado dueno = new UsuarioAutenticado(UUID.randomUUID(), taller.valor(), "Dueño", TipoDeCuenta.PERSONAL, Rol.DUENO);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(dueno, null, List.of()));
    }

    private TallerId crearTaller(String prefijo) {
        UUID id = UUID.randomUUID();
        String slug = prefijo + id.toString().substring(0, 8);
        jdbc.update("insert into talleres (id, nombre, slug, estado) values (?, ?, ?, 'ACTIVO')", id, "Taller " + slug, slug);
        return TallerId.de(id);
    }
}
