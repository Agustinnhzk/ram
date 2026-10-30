package com.ram.reparaciones.adaptadores.web;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorDeIntentosTest {

    private final RelojQueAvanza reloj = new RelojQueAvanza();
    private final LimitadorDeIntentos limitador = new LimitadorDeIntentos(reloj);

    @Test
    void despuesDeCincoFallosLaOrdenQuedaBloqueadaAunqueCambieLaIp() {
        for (int i = 0; i < LimitadorDeIntentos.FALLOS_POR_ORDEN; i++) {
            assertThat(limitador.puedeIntentar("ip-" + i, "ram:901")).isTrue();
            limitador.registrarFallo("ram:901");
        }

        assertThat(limitador.puedeIntentar("otra-ip", "ram:901")).isFalse();
        assertThat(limitador.puedeIntentar("otra-ip", "ram:902")).isTrue(); // las demás órdenes siguen andando
    }

    @Test
    void elBloqueoSeLevantaDespuesDeLaVentana() {
        for (int i = 0; i < LimitadorDeIntentos.FALLOS_POR_ORDEN; i++) {
            limitador.puedeIntentar("ip", "ram:901");
            limitador.registrarFallo("ram:901");
        }
        reloj.avanzar(LimitadorDeIntentos.VENTANA.plusSeconds(1));

        assertThat(limitador.puedeIntentar("ip", "ram:901")).isTrue();
    }

    @Test
    void acertarBorraLosFallosAnteriores() {
        for (int i = 0; i < LimitadorDeIntentos.FALLOS_POR_ORDEN - 1; i++) {
            limitador.puedeIntentar("ip", "ram:901");
            limitador.registrarFallo("ram:901");
        }
        limitador.registrarExito("ram:901");
        limitador.registrarFallo("ram:901");

        assertThat(limitador.puedeIntentar("ip", "ram:901")).isTrue();
    }

    @Test
    void unaIpNoPuedeBarrerMuchasOrdenes() {
        for (int i = 0; i < LimitadorDeIntentos.INTENTOS_POR_IP; i++) {
            assertThat(limitador.puedeIntentar("1.2.3.4", "ram:" + i)).isTrue();
        }

        assertThat(limitador.puedeIntentar("1.2.3.4", "ram:999")).isFalse();
        assertThat(limitador.puedeIntentar("5.6.7.8", "ram:999")).isTrue();
    }

    private static final class RelojQueAvanza extends Clock {

        private Instant ahora = Instant.parse("2026-10-29T15:00:00Z");

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
