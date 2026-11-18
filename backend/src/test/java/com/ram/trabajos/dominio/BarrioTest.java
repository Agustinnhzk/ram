package com.ram.trabajos.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarrioTest {

    @Test
    void lasDistintasFormasDeEscribirElMismoBarrioSonLaMismaZona() {
        assertThat(Barrio.de("Alberdi").clave()).isEqualTo("alberdi");
        assertThat(Barrio.de("B° Alberdi").clave()).isEqualTo("alberdi");
        assertThat(Barrio.de("bº alberdi").clave()).isEqualTo("alberdi");
        assertThat(Barrio.de("  barrio   ALBERDI ").clave()).isEqualTo("alberdi");
        assertThat(Barrio.de("B°Alberdi").clave()).isEqualTo("alberdi");
        assertThat(Barrio.de("Echesortu").clave()).isEqualTo(Barrio.de("echesortu").clave());
    }

    @Test
    void seMuestraSinElPrefijo_yConMayusculasSiSeEscribioEnMinuscula() {
        assertThat(Barrio.de("b° alberdi").texto()).isEqualTo("Alberdi");
        assertThat(Barrio.de("villa gobernador galvez").texto()).isEqualTo("Villa Gobernador Galvez");
        assertThat(Barrio.de("barrio república de la sexta").texto()).isEqualTo("República de la Sexta");
        // Si ya tiene mayúsculas, se respeta como se escribió.
        assertThat(Barrio.de("Alberdi (zona norte)").texto()).isEqualTo("Alberdi (zona norte)");
        assertThat(Barrio.de("Funes").texto()).isEqualTo("Funes");
    }

    @Test
    void noAceptaUnaDireccion_niUnBarrioVacio() {
        assertThatThrownBy(() -> Barrio.de("Mendoza 1234")).hasMessageContaining("sin calle ni altura");
        assertThatThrownBy(() -> Barrio.de(" ")).hasMessageContaining("barrio o la localidad");
        assertThatThrownBy(() -> Barrio.de("B° ")).hasMessageContaining("barrio o la localidad");
        assertThatThrownBy(() -> Barrio.de("x".repeat(61))).hasMessageContaining("60");
    }
}
