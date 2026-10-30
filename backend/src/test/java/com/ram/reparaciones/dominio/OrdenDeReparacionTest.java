package com.ram.reparaciones.dominio;

import com.ram.clientes.ClienteId;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.TransicionInvalidaException;
import com.ram.usuarios.UsuarioId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;

import static com.ram.reparaciones.EstadoReparacion.APROBADO;
import static com.ram.reparaciones.EstadoReparacion.ENTREGADO;
import static com.ram.reparaciones.EstadoReparacion.EN_DIAGNOSTICO;
import static com.ram.reparaciones.EstadoReparacion.EN_REPARACION;
import static com.ram.reparaciones.EstadoReparacion.ESPERANDO_REPUESTO;
import static com.ram.reparaciones.EstadoReparacion.INGRESADO;
import static com.ram.reparaciones.EstadoReparacion.LISTO_PARA_RETIRAR;
import static com.ram.reparaciones.EstadoReparacion.PRESUPUESTADO;
import static com.ram.reparaciones.EstadoReparacion.RECHAZADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdenDeReparacionTest {

    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Autor AGUSTIN = new Autor(UsuarioId.nuevo(), "Agustín");
    private static final Instant AHORA = Instant.parse("2026-10-29T15:00:00Z");

    // ─── El recorrido ───────────────────────────────────────────────────────────────────────────

    @Test
    void todoEstadoSePuedeAlcanzarDesdeElIngreso() {
        assertThat(alcanzablesDesde(INGRESADO)).isEqualTo(EnumSet.allOf(EstadoReparacion.class));
    }

    @Test
    void desdeCualquierEstadoSePuedeLlegarALaEntrega() {
        // Ningún equipo puede quedar "trabado" en un estado sin salida.
        for (EstadoReparacion estado : EstadoReparacion.values()) {
            assertThat(alcanzablesDesde(estado)).as("desde " + estado).contains(ENTREGADO);
        }
    }

    @Test
    void entregadoEsElFinal() {
        assertThat(ENTREGADO.siguientes()).isEmpty();
        assertThat(ENTREGADO.enElTaller()).isFalse();
    }

    // ─── La orden ───────────────────────────────────────────────────────────────────────────────

    @Test
    void elIngresoDejaLaOrdenIngresadaConSuPrimerMovimientoVisibleParaElCliente() {
        OrdenDeReparacion orden = nueva();

        assertThat(orden.estado()).isEqualTo(INGRESADO);
        assertThat(orden.movimientos()).singleElement().satisfies(m -> {
            assertThat(m.estado()).isEqualTo(INGRESADO);
            assertThat(m.paraCliente()).isTrue();
            assertThat(m.autor().nombre()).isEqualTo("Agustín");
        });
    }

    @Test
    void noSePuedeSaltearPasos() {
        OrdenDeReparacion orden = nueva();

        assertThatThrownBy(() -> orden.cambiarEstado(EN_REPARACION, null, false, AGUSTIN, AHORA))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThat(orden.estado()).isEqualTo(INGRESADO);
    }

    @Test
    void presupuestarYEntregarTienenSusPropiosMetodos() {
        OrdenDeReparacion orden = nueva();
        orden.cambiarEstado(EN_DIAGNOSTICO, null, false, AGUSTIN, AHORA);

        assertThatThrownBy(() -> orden.cambiarEstado(PRESUPUESTADO, null, false, AGUSTIN, AHORA))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void elRecorridoCompletoQuedaEnLaBitacora() {
        OrdenDeReparacion orden = nueva();
        orden.cambiarEstado(EN_DIAGNOSTICO, "Revisando la bisagra", false, AGUSTIN, AHORA);
        orden.presupuestar(110_000, "Cambio de bisagra y carcasa", AGUSTIN, AHORA);
        orden.cambiarEstado(APROBADO, null, true, AGUSTIN, AHORA);
        orden.cambiarEstado(EN_REPARACION, null, true, AGUSTIN, AHORA);
        orden.cambiarEstado(ESPERANDO_REPUESTO, "Llega el lunes", true, AGUSTIN, AHORA);
        orden.cambiarEstado(EN_REPARACION, null, true, AGUSTIN, AHORA);
        orden.cambiarEstado(LISTO_PARA_RETIRAR, null, true, AGUSTIN, AHORA);
        orden.entregar(60, null, AGUSTIN, AHORA, ARGENTINA);

        assertThat(orden.estado()).isEqualTo(ENTREGADO);
        assertThat(orden.presupuestoMonto()).isEqualTo(110_000L);
        // 9 pasos + 1 porque la nota del diagnóstico era interna: el cambio de estado y la nota van separados.
        assertThat(orden.movimientos()).hasSize(10);
        assertThat(orden.movimientos().get(1).estado()).isEqualTo(EN_DIAGNOSTICO);
        assertThat(orden.movimientos().get(1).paraCliente()).isTrue();
        assertThat(orden.movimientos().get(2).nota()).isEqualTo("Revisando la bisagra");
        assertThat(orden.movimientos().get(2).paraCliente()).isFalse();
    }

    @Test
    void elPresupuestoSePuedeCorregirMientrasNoHayRespuesta() {
        OrdenDeReparacion orden = nueva();
        orden.cambiarEstado(EN_DIAGNOSTICO, null, false, AGUSTIN, AHORA);
        orden.presupuestar(100_000, "Pantalla", AGUSTIN, AHORA);

        orden.presupuestar(95_000, "Pantalla (con descuento)", AGUSTIN, AHORA);

        assertThat(orden.estado()).isEqualTo(PRESUPUESTADO);
        assertThat(orden.presupuestoMonto()).isEqualTo(95_000L);
        assertThat(orden.movimientos().getLast().nota()).isEqualTo("Se corrigió el presupuesto");
    }

    @Test
    void unPresupuestoRechazadoSeRetiraSinCosto() {
        OrdenDeReparacion orden = nueva();
        orden.cambiarEstado(EN_DIAGNOSTICO, null, false, AGUSTIN, AHORA);
        orden.presupuestar(100_000, "Pantalla", AGUSTIN, AHORA);
        orden.cambiarEstado(RECHAZADO, "No le convino", false, AGUSTIN, AHORA);
        orden.cambiarEstado(LISTO_PARA_RETIRAR, null, true, AGUSTIN, AHORA);
        orden.entregar(0, null, AGUSTIN, AHORA, ARGENTINA);

        assertThat(orden.garantiaHasta()).isNull();
        assertThat(orden.enGarantia(LocalDate.of(2026, 10, 30))).isFalse();
    }

    @Test
    void laGarantiaSeCuentaDesdeLaFechaDeArgentina() {
        // 30/10 a las 02:00 UTC es todavía el 29/10 a las 23:00 en Argentina: la garantía arranca el 29.
        OrdenDeReparacion orden = listaParaRetirar();
        orden.entregar(60, null, AGUSTIN, Instant.parse("2026-10-30T02:00:00Z"), ARGENTINA);

        assertThat(orden.garantiaHasta()).isEqualTo(LocalDate.of(2026, 12, 28));
        assertThat(orden.enGarantia(LocalDate.of(2026, 12, 28))).isTrue();  // el último día cuenta
        assertThat(orden.enGarantia(LocalDate.of(2026, 12, 29))).isFalse();
    }

    @Test
    void laGarantiaTieneQueSerRazonable() {
        assertThatThrownBy(() -> listaParaRetirar().entregar(-1, null, AGUSTIN, AHORA, ARGENTINA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> listaParaRetirar().entregar(5_000, null, AGUSTIN, AHORA, ARGENTINA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validaLosDatosDelIngreso() {
        ClienteId cliente = ClienteId.nuevo();
        Equipo equipo = new Equipo("Notebook Lenovo", true, null);
        assertThatThrownBy(() -> OrdenDeReparacion.ingresar(0, cliente, equipo, null, "No prende", null, AGUSTIN, AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OrdenDeReparacion.ingresar(900, cliente, equipo, null, "   ", null, AGUSTIN, AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Equipo(" ", false, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> nueva().agregarNota("  ", true, AGUSTIN, AHORA)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elPasswordNoApareceAlImprimirLaOrden() {
        OrdenDeReparacion orden = OrdenDeReparacion.ingresar(900, ClienteId.nuevo(), new Equipo("Notebook", false, null),
                "secreto123", "No prende", null, AGUSTIN, AHORA);

        assertThat(orden.toString()).doesNotContain("secreto123");
    }

    // ─── Ayudas ─────────────────────────────────────────────────────────────────────────────────

    private static OrdenDeReparacion nueva() {
        return OrdenDeReparacion.ingresar(900, ClienteId.nuevo(), new Equipo("Notebook Lenovo IdeaPad 3", true, "Ryzen 5"),
                "1234", "No prende", "Rayón en la tapa", AGUSTIN, AHORA);
    }

    private static OrdenDeReparacion listaParaRetirar() {
        OrdenDeReparacion orden = nueva();
        orden.cambiarEstado(EN_DIAGNOSTICO, null, false, AGUSTIN, AHORA);
        orden.presupuestar(50_000, "Limpieza", AGUSTIN, AHORA);
        orden.cambiarEstado(APROBADO, null, true, AGUSTIN, AHORA);
        orden.cambiarEstado(EN_REPARACION, null, true, AGUSTIN, AHORA);
        orden.cambiarEstado(LISTO_PARA_RETIRAR, null, true, AGUSTIN, AHORA);
        return orden;
    }

    /** Todos los estados a los que se puede llegar desde uno, siguiendo las transiciones (recorrido en anchura). */
    private static Set<EstadoReparacion> alcanzablesDesde(EstadoReparacion inicio) {
        Set<EstadoReparacion> vistos = EnumSet.of(inicio);
        Deque<EstadoReparacion> pendientes = new ArrayDeque<>(Set.of(inicio));
        while (!pendientes.isEmpty()) {
            for (EstadoReparacion siguiente : pendientes.poll().siguientes()) {
                if (vistos.add(siguiente)) {
                    pendientes.add(siguiente);
                }
            }
        }
        return vistos;
    }
}
