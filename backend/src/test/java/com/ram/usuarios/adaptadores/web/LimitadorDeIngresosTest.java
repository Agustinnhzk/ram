package com.ram.usuarios.adaptadores.web;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorDeIngresosTest {

    private Instant ahora = Instant.parse("2026-10-30T12:00:00Z");
    private final Clock reloj = new Clock() {
        public ZoneId getZone() { return ZoneId.of("UTC"); }
        public Clock withZone(ZoneId zona) { return this; }
        public Instant instant() { return ahora; }
    };
    private final LimitadorDeIngresos limitador = new LimitadorDeIngresos(reloj);

    @Test
    void despuesDeCincoFallosConElMismoCorreoFrena_yA15MinutosSeLibera() {
        for (int i = 0; i < 5; i++) {
            assertThat(limitador.puedeIntentar("1.1.1.1", "ana@mail.com")).isTrue();
            limitador.registrarFallo("1.1.1.1", "ana@mail.com");
        }
        assertThat(limitador.puedeIntentar("2.2.2.2", "ANA@mail.com ")).isFalse(); // otra IP, mismo correo
        assertThat(limitador.puedeIntentar("1.1.1.1", "otro@mail.com")).isTrue();

        ahora = ahora.plus(Duration.ofMinutes(16));
        assertThat(limitador.puedeIntentar("1.1.1.1", "ana@mail.com")).isTrue();
    }

    @Test
    void entrarBienBorraLosFallosDeEseCorreo() {
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("1.1.1.1", "ana@mail.com");
        }
        limitador.registrarExito("ana@mail.com");
        limitador.registrarFallo("1.1.1.1", "ana@mail.com");
        assertThat(limitador.puedeIntentar("1.1.1.1", "ana@mail.com")).isTrue();
    }

    @Test
    void unaIpQuePruebaMuchosCorreosTambienFrena() {
        for (int i = 0; i < 20; i++) {
            limitador.registrarFallo("6.6.6.6", "correo" + i + "@mail.com");
        }
        assertThat(limitador.puedeIntentar("6.6.6.6", "nuevo@mail.com")).isFalse();
    }

    @Test
    void protegerAnotaElFalloYFrenaAntesDeProbar() {
        for (int i = 0; i < 5; i++) {
            try {
                limitador.proteger("1.1.1.1", "ana@mail.com", () -> {
                    throw new com.ram.usuarios.aplicacion.CredencialesInvalidasException();
                });
            } catch (com.ram.usuarios.aplicacion.CredencialesInvalidasException esperado) {
                // la clave estaba mal
            }
        }
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> limitador.proteger("1.1.1.1", "ana@mail.com", () -> "no llega"))
                .isInstanceOf(DemasiadosIntentosException.class);
    }
}
