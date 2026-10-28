package com.ram.clientes.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatosDeContactoTest {

    @Test
    void elNombreSeGuardaProlijoYSeBuscaSinTildesNiMayusculas() {
        NombreCompleto nombre = new NombreCompleto("  Agustín   Pérez ");

        assertThat(nombre.valor()).isEqualTo("Agustín Pérez");
        assertThat(nombre.paraBuscar()).isEqualTo("agustin perez");
        assertThat(NombreCompleto.normalizar("AGUSTIN perez")).isEqualTo(nombre.paraBuscar());
        assertThat(NombreCompleto.normalizar("Muñoz")).isEqualTo("munoz");
    }

    @Test
    void laNormalizacionNoDejaComodinesDeSql() {
        assertThat(NombreCompleto.normalizar("%_juan%")).isEqualTo("juan");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "1234"})
    void rechazaNombresSinLetras(String nombre) {
        assertThatThrownBy(() -> new NombreCompleto(nombre)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elTelefonoQuedaSoloConNumeros() {
        assertThat(new Telefono("341 555-1234").valor()).isEqualTo("3415551234");
        assertThat(new Telefono("(0341) 555.1234").valor()).isEqualTo("03415551234");
        assertThat(new Telefono("+54 9 341 555 1234").valor()).isEqualTo("+5493415551234");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abc12345", "3415551234567890", "341-555-12x4"})
    void rechazaTelefonosInvalidos(String telefono) {
        assertThatThrownBy(() -> new Telefono(telefono)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elCorreoEsOpcional() {
        assertThat(Correo.opcional(null)).isEmpty();
        assertThat(Correo.opcional("  ")).isEmpty();
        assertThat(Correo.opcional(" Cliente@Mail.com ")).contains(new Correo("cliente@mail.com"));
    }

    @Test
    void siHayCorreoTieneQueSerValido() {
        assertThatThrownBy(() -> Correo.opcional("cliente@")).isInstanceOf(IllegalArgumentException.class);
    }
}
