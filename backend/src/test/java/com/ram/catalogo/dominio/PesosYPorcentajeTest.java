package com.ram.catalogo.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PesosYPorcentajeTest {

    @Test
    void unPrecioNoPuedeSerNegativoNiAbsurdo() {
        assertThatThrownBy(() -> new Pesos(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pesos(Pesos.MAXIMO + 1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Pesos(0).valor()).isZero();
    }

    @Test
    void unAumentoRedondeaAlPesoMasCercano() {
        // 41.237 × 1,08 = 44.535,96
        assertThat(new Pesos(41_237).aplicar(porcentaje("8"))).isEqualTo(new Pesos(44_536));
    }

    @Test
    void lasMitadesRedondeanParaArriba() {
        // 5 × 1,10 = 5,5
        assertThat(new Pesos(5).aplicar(porcentaje("10"))).isEqualTo(new Pesos(6));
    }

    @Test
    void tambienSePuedeRebajar() {
        assertThat(new Pesos(1_000).aplicar(porcentaje("-10"))).isEqualTo(new Pesos(900));
    }

    @Test
    void aceptaPorcentajesConDecimales() {
        // 200.000 × 1,085 = 217.000
        assertThat(new Pesos(200_000).aplicar(porcentaje("8.5"))).isEqualTo(new Pesos(217_000));
    }

    @Test
    void rechazaPorcentajesQueSonErroresDeTipeo() {
        assertThatThrownBy(() -> porcentaje("0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> porcentaje("800")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> porcentaje("-100")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> porcentaje("8.555")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Porcentaje(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elBuscadorIgnoraTildesMayusculasYSimbolos() {
        assertThat(TextoDeBusqueda.normalizar("  Cámaras + DVR!! ")).isEqualTo("camaras dvr");
        assertThat(TextoDeBusqueda.palabras("Kit  DVR")).containsExactly("kit", "dvr");
        assertThat(TextoDeBusqueda.palabras("   ")).isEmpty();
    }

    private static Porcentaje porcentaje(String valor) {
        return new Porcentaje(new BigDecimal(valor));
    }
}
