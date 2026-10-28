package com.ram.usuarios.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaveYCorreoTest {

    @Test
    void elCorreoSeGuardaEnMinusculas() {
        assertThat(new CorreoDeAcceso("  Agus@Mail.COM ").valor()).isEqualTo("agus@mail.com");
    }

    @Test
    void unCorreoInvalidoEnElLoginNoEsUnError() {
        assertThat(CorreoDeAcceso.intentar("no-es-un-correo")).isEmpty();
        assertThat(CorreoDeAcceso.intentar(null)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "        ", "corta"})
    void rechazaClavesVaciasOCortas(String clave) {
        assertThatThrownBy(() -> new ClaveNueva(clave)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaClavesDeMasDe72Bytes() {
        // 40 "ñ" son 40 caracteres pero 80 bytes: BCrypt ignoraría el final.
        assertThatThrownBy(() -> new ClaveNueva("ñ".repeat(40))).isInstanceOf(IllegalArgumentException.class);
        assertThat(new ClaveNueva("a".repeat(72)).valor()).hasSize(72);
    }

    @Test
    void laClaveNoApareceEnLosLogs() {
        assertThat(new ClaveNueva("mi-clave-secreta").toString()).doesNotContain("mi-clave-secreta");
    }
}
