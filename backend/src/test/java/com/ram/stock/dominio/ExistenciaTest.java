package com.ram.stock.dominio;

import com.ram.catalogo.ProductoId;
import com.ram.stock.OperacionDeStockInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExistenciaTest {

    private final ProductoId producto = ProductoId.nuevo();

    @Test
    void descontarSacaLoQueHay_yNuncaQuedaNegativo() {
        Existencia e = Existencia.empezar(producto, 2);

        assertThat(e.descontarLoQueHaya(3)).isEqualTo(2);
        assertThat(e.cantidad()).isZero();
        assertThat(e.descontarLoQueHaya(1)).isZero();
        assertThat(e.cantidad()).isZero();
    }

    @Test
    void sacarParaUnaReservaNecesitaTodasLasUnidades() {
        Existencia e = Existencia.empezar(producto, 1);

        assertThatThrownBy(() -> e.sacar(2))
                .isInstanceOf(OperacionDeStockInvalidaException.class)
                .hasMessageContaining("falta 1 unidad");
        assertThat(e.cantidad()).isEqualTo(1);
        e.sacar(1);
        assertThat(e.cantidad()).isZero();
    }

    @Test
    void ajustarDevuelveLaDiferencia() {
        Existencia e = Existencia.empezar(producto, 5);

        assertThat(e.ajustar(3)).isEqualTo(-2);
        assertThat(e.ajustar(7)).isEqualTo(4);
        assertThat(e.cantidad()).isEqualTo(7);
    }

    @Test
    void rechazaCantidadesImposibles() {
        assertThatThrownBy(() -> Existencia.empezar(producto, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Existencia.empezar(producto, Existencia.MAXIMO + 1)).isInstanceOf(IllegalArgumentException.class);
        Existencia e = Existencia.empezar(producto, 0);
        assertThatThrownBy(() -> e.ingresar(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> e.ajustar(-3)).isInstanceOf(IllegalArgumentException.class);
    }
}
