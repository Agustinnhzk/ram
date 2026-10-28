package com.ram.clientes.adaptadores.persistencia;

import com.ram.TestcontainersConfiguration;
import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.NuevoCliente;
import com.ram.tenants.TallerId;
import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EL test más importante de la plataforma (ADR-002 y ADR-005): un taller nunca ve datos de otro.
 * Corre contra un PostgreSQL real, porque lo que se prueba es el filtro que agrega Hibernate.
 * Todo módulo nuevo con datos por taller debería sumar un test como este.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AislamientoEntreTalleresTests {

    @Autowired
    private Clientes clientes;

    @Autowired
    private JdbcTemplate jdbc;

    private TallerId tallerA;
    private TallerId tallerB;

    @BeforeEach
    void crearDosTalleres() {
        tallerA = crearTaller("a");
        tallerB = crearTaller("b");
    }

    @Test
    void unTallerNoEncuentraLosClientesDeOtro() {
        ContextoTaller.con(tallerA, () ->
                clientes.registrar(new NuevoCliente("Cliente Secreto", "3415551234", null)));

        assertThat(ContextoTaller.con(tallerA, () -> clientes.buscarPorNombre("secreto"))).hasSize(1);
        assertThat(ContextoTaller.con(tallerB, () -> clientes.buscarPorNombre("secreto"))).isEmpty();
    }

    @Test
    void unTallerNoPuedeLeerUnClienteDeOtroAunqueConozcaSuId() {
        ClienteId id = ContextoTaller.con(tallerA, () ->
                clientes.registrar(new NuevoCliente("Cliente Secreto", "3415551234", null)));

        assertThat(ContextoTaller.con(tallerB, () -> clientes.obtener(id))).isEmpty();
    }

    @Test
    void sinTallerEnElContextoNoSeVeNada() {
        ContextoTaller.con(tallerA, () ->
                clientes.registrar(new NuevoCliente("Cliente Secreto", "3415551234", null)));

        // Fallar cerrado: si algún código se olvida del contexto, no ve datos de nadie.
        assertThat(clientes.buscarPorNombre("secreto")).isEmpty();
    }

    @Test
    void elClienteQuedaGuardadoConElTallerQueLoCreo() {
        ClienteId id = ContextoTaller.con(tallerA, () ->
                clientes.registrar(new NuevoCliente("Ana López", "3415551234", null)));

        UUID tallerGuardado = jdbc.queryForObject(
                "select taller_id from clientes where id = ?", UUID.class, id.valor());
        assertThat(tallerGuardado).isEqualTo(tallerA.valor());
    }

    /** Se inserta directo por SQL: este test es de clientes y no debería depender del módulo tenants por dentro. */
    private TallerId crearTaller(String prefijo) {
        UUID id = UUID.randomUUID();
        String slug = prefijo + id.toString().substring(0, 8);
        jdbc.update("insert into talleres (id, nombre, slug, estado) values (?, ?, ?, 'ACTIVO')",
                id, "Taller " + slug, slug);
        return TallerId.de(id);
    }
}
