package com.ram.contactos.adaptadores.web;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorDeEnviosTest {

    private Instant ahora = Instant.parse("2026-10-29T12:00:00Z");
    private final Clock reloj = new Clock() {
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
    };

    @Test
    void cincoEnviosPorHoraDesdeLaMismaIp() {
        LimitadorDeEnvios limitador = new LimitadorDeEnvios(reloj);
        for (int i = 0; i < LimitadorDeEnvios.ENVIOS_POR_IP; i++) {
            assertThat(limitador.puedeEnviar("1.1.1.1")).isTrue();
        }
        assertThat(limitador.puedeEnviar("1.1.1.1")).isFalse();
        assertThat(limitador.puedeEnviar("2.2.2.2")).as("otra IP no se ve afectada").isTrue();

        ahora = ahora.plus(Duration.ofMinutes(61));
        assertThat(limitador.puedeEnviar("1.1.1.1")).as("pasada la hora, puede de nuevo").isTrue();
    }
}
