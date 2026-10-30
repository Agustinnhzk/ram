package com.ram.reparaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.NuevoCliente;
import com.ram.reparaciones.DetalleDeOrden;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.NuevoIngreso;
import com.ram.reparaciones.NumeroDeOrdenRepetidoException;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.OrdenNoEncontradaException;
import com.ram.reparaciones.ResumenDeOrden;
import com.ram.reparaciones.Seguimiento;
import com.ram.reparaciones.VistaDeOrdenes;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.ram.reparaciones.EstadoReparacion.APROBADO;
import static com.ram.reparaciones.EstadoReparacion.EN_DIAGNOSTICO;
import static com.ram.reparaciones.EstadoReparacion.EN_REPARACION;
import static com.ram.reparaciones.EstadoReparacion.LISTO_PARA_RETIRAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioReparacionesTest {

    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final DoblesDeReparaciones.OrdenesEnMemoria ordenes = new DoblesDeReparaciones.OrdenesEnMemoria();
    private final DoblesDeReparaciones.ClientesEnMemoria clientes = new DoblesDeReparaciones.ClientesEnMemoria();
    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC);
    private UsuarioAutenticado logueado;
    private ServicioReparaciones servicio;

    @BeforeEach
    void preparar() {
        logueado = usuario(Rol.DUENO);
        SesionActual sesion = () -> Optional.ofNullable(logueado);
        servicio = new ServicioReparaciones(ordenes, clientes, sesion, reloj, ARGENTINA);
    }

    @Test
    void ingresaUnEquipoDeUnClienteNuevo() {
        OrdenId id = servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "341 555-1234", null)));

        DetalleDeOrden detalle = servicio.obtener(id).orElseThrow();
        assertThat(detalle.numero()).isEqualTo(900);
        assertThat(detalle.cliente().nombreCompleto()).isEqualTo("Ana López");
        assertThat(detalle.estado()).isEqualTo(EstadoReparacion.INGRESADO);
        assertThat(detalle.movimientos()).singleElement().satisfies(m -> assertThat(m.usuario()).isEqualTo("Agustín"));
        assertThat(servicio.ultimoNumero().getAsInt()).isEqualTo(900);
    }

    @Test
    void unClienteQueVuelveNoSeDuplica() {
        ClienteId ana = clientes.registrar(new NuevoCliente("Ana López", "3415551234", null));

        servicio.registrarIngreso(ingreso(901, ana.valor(), null));

        assertThat(clientes.cantidad()).isEqualTo(1);
    }

    @Test
    void elNumeroDeOrdenNoSePuedeRepetir() {
        servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "3415551234", null)));

        assertThatThrownBy(() -> servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Juan", "3416661234", null))))
                .isInstanceOf(NumeroDeOrdenRepetidoException.class);
    }

    @Test
    void sinClienteONumeroNoHayIngreso() {
        assertThatThrownBy(() -> servicio.registrarIngreso(ingreso(900, null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.registrarIngreso(ingreso(null, null, new NuevoCliente("Ana", "3415551234", null))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.registrarIngreso(ingreso(900, UUID.randomUUID(), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elMostradorNoVeElPasswordDelEquipo() {
        OrdenId id = servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "3415551234", null)));

        assertThat(servicio.obtener(id).orElseThrow().password()).isEqualTo("1234");
        logueado = usuario(Rol.TECNICO);
        assertThat(servicio.obtener(id).orElseThrow().password()).isEqualTo("1234");
        logueado = usuario(Rol.MOSTRADOR);
        DetalleDeOrden paraMostrador = servicio.obtener(id).orElseThrow();
        assertThat(paraMostrador.password()).isNull();
        assertThat(paraMostrador.passwordVisible()).isFalse();
    }

    @Test
    void elListadoDelDiaMuestraLoQueEstaEnElTaller() {
        servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "3415551234", null)));
        OrdenId entregada = servicio.registrarIngreso(ingreso(901, null, new NuevoCliente("Juan Gómez", "3416661234", null)));
        llevarAEntregada(entregada, 60);

        assertThat(servicio.listar(VistaDeOrdenes.EN_TALLER, null)).extracting(ResumenDeOrden::numero).containsExactly(900);
        assertThat(servicio.listar(VistaDeOrdenes.ENTREGADAS, null)).singleElement().satisfies(r -> {
            assertThat(r.numero()).isEqualTo(901);
            assertThat(r.cliente()).isEqualTo("Juan Gómez");
            assertThat(r.enGarantia()).isTrue();
            assertThat(r.garantiaHasta()).isEqualTo(LocalDate.of(2026, 12, 28));
        });
        assertThat(servicio.listar(VistaDeOrdenes.TODAS, null)).extracting(ResumenDeOrden::numero).containsExactly(901, 900);
    }

    @Test
    void seBuscaPorNumeroOPorNombreDelCliente() {
        servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "3415551234", null)));
        servicio.registrarIngreso(ingreso(901, null, new NuevoCliente("Juan Gómez", "3416661234", null)));

        assertThat(servicio.listar(VistaDeOrdenes.TODAS, "901")).extracting(ResumenDeOrden::cliente).containsExactly("Juan Gómez");
        assertThat(servicio.listar(VistaDeOrdenes.TODAS, "lópez")).extracting(ResumenDeOrden::numero).containsExactly(900);
        assertThat(servicio.listar(VistaDeOrdenes.TODAS, "pedro")).isEmpty();
    }

    @Test
    void elClienteSigueSuReparacionConElNumeroYLosUltimosCuatroDigitos() {
        OrdenId id = servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "341 555-1234", null)));
        servicio.cambiarEstado(id, EN_DIAGNOSTICO, "El cliente es complicado", false);
        servicio.agregarNota(id, "Ya estamos revisando tu equipo", true);

        Seguimiento seguimiento = servicio.seguimiento(900, "1234").orElseThrow();

        assertThat(seguimiento.estado()).isEqualTo(EN_DIAGNOSTICO);
        // Ve el ingreso, el cambio a diagnóstico y el aviso. La nota interna ("complicado") no.
        assertThat(seguimiento.pasos()).extracting(Seguimiento.Paso::estado)
                .containsExactly(EstadoReparacion.INGRESADO, EN_DIAGNOSTICO, null);
        assertThat(seguimiento.pasos()).extracting(Seguimiento.Paso::nota)
                .containsExactly(null, null, "Ya estamos revisando tu equipo");
        assertThat(seguimiento.toString()).doesNotContain("complicado", "Rayón");
    }

    @Test
    void conDigitosEquivocadosNoHaySeguimiento() {
        servicio.registrarIngreso(ingreso(900, null, new NuevoCliente("Ana López", "3415551234", null)));

        assertThat(servicio.seguimiento(900, "9999")).isEmpty();
        assertThat(servicio.seguimiento(901, "1234")).isEmpty();
        assertThat(servicio.seguimiento(900, "234")).isEmpty();
        assertThat(servicio.seguimiento(900, null)).isEmpty();
    }

    @Test
    void noSeMueveUnaOrdenQueNoExiste() {
        assertThatThrownBy(() -> servicio.cambiarEstado(OrdenId.nueva(), EN_DIAGNOSTICO, null, false))
                .isInstanceOf(OrdenNoEncontradaException.class);
    }

    // ─── Ayudas ─────────────────────────────────────────────────────────────────────────────────

    @Test
    void laFichaMuestraLasOrdenesDelCliente_yElClienteLasVeEnLaTiendaSiSuCuentaEstaVinculada() {
        ClienteId ana = clientes.registrar(new NuevoCliente("Ana López", "3415551234", null));
        servicio.registrarIngreso(ingreso(900, ana.valor(), null));
        servicio.registrarIngreso(ingreso(901, ana.valor(), null));
        servicio.registrarIngreso(ingreso(902, null, new NuevoCliente("Otro", "3416661234", null)));

        assertThat(servicio.deCliente(ana)).extracting(r -> r.numero()).containsExactly(901, 900);

        UUID cuenta = UUID.randomUUID();
        logueado = new UsuarioAutenticado(cuenta, UUID.randomUUID(), "Ana", TipoDeCuenta.CLIENTE, null);
        assertThat(servicio.misReparaciones()).isEmpty(); // todavía sin vincular

        clientes.cuentas.put(cuenta, ana);
        assertThat(servicio.misReparaciones()).extracting(s -> s.numero()).containsExactly(901, 900);
    }

    private void llevarAEntregada(OrdenId id, int garantia) {
        servicio.cambiarEstado(id, EN_DIAGNOSTICO, null, false);
        servicio.presupuestar(id, 50_000, "Limpieza");
        servicio.cambiarEstado(id, APROBADO, null, true);
        servicio.cambiarEstado(id, EN_REPARACION, null, true);
        servicio.cambiarEstado(id, LISTO_PARA_RETIRAR, null, true);
        servicio.entregar(id, garantia, null);
    }

    private static NuevoIngreso ingreso(Integer numero, UUID clienteId, NuevoCliente nuevo) {
        return new NuevoIngreso(numero, clienteId, nuevo, "Notebook Lenovo IdeaPad 3", true, "Ryzen 5", "1234",
                "No prende", "Rayón en la tapa");
    }

    private static UsuarioAutenticado usuario(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín", TipoDeCuenta.PERSONAL, rol);
    }
}
