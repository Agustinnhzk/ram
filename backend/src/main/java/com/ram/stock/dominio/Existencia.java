package com.ram.stock.dominio;

import com.ram.catalogo.ProductoId;
import com.ram.stock.OperacionDeStockInvalidaException;

import java.util.Objects;

/**
 * Cuántas unidades hay de un producto en el taller. <b>Nunca es negativa</b>: lo que se vende sin stock
 * no "debe" unidades, queda como {@link Reserva} (ADR-014).
 *
 * <p>Cada operación devuelve cuánto cambió, para que el servicio anote el movimiento.
 */
public final class Existencia {

    /** Un tope razonable para un taller: frena un "100000000" tipeado de más. */
    public static final int MAXIMO = 100_000;

    private final ProductoId producto;
    private int cantidad;

    private Existencia(ProductoId producto, int cantidad) {
        this.producto = Objects.requireNonNull(producto);
        this.cantidad = cantidad;
    }

    public static Existencia empezar(ProductoId producto, int cantidadInicial) {
        validar(cantidadInicial, "La cantidad inicial");
        return new Existencia(producto, cantidadInicial);
    }

    public static Existencia reconstruir(ProductoId producto, int cantidad) {
        return new Existencia(producto, cantidad);
    }

    /** Llegó mercadería. */
    public void ingresar(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Tiene que ingresar al menos una unidad");
        }
        validar(cantidad + unidades, "El stock");
        cantidad += unidades;
    }

    /** @return la diferencia (+ o −) con lo que había */
    public int ajustar(int cantidadContada) {
        validar(cantidadContada, "La cantidad contada");
        int diferencia = cantidadContada - cantidad;
        cantidad = cantidadContada;
        return diferencia;
    }

    /**
     * Saca hasta {@code pedidas} unidades: las que haya. Es lo que pasa al confirmar un pago.
     *
     * @return cuántas se sacaron (de 0 a pedidas). El resto, el servicio lo reserva.
     */
    public int descontarLoQueHaya(int pedidas) {
        if (pedidas <= 0) {
            throw new IllegalArgumentException("Hay que descontar al menos una unidad");
        }
        int sacadas = Math.min(cantidad, pedidas);
        cantidad -= sacadas;
        return sacadas;
    }

    /** Saca exactamente esas unidades (para cubrir una reserva): si no hay todas, no saca ninguna. */
    public void sacar(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Hay que sacar al menos una unidad");
        }
        if (unidades > cantidad) {
            int faltan = unidades - cantidad;
            throw new OperacionDeStockInvalidaException("No alcanza el stock: falta" + (faltan == 1 ? " 1 unidad" : "n " + faltan + " unidades")
                    + ". Cargá la mercadería primero.");
        }
        cantidad -= unidades;
    }

    private static void validar(int valor, String que) {
        if (valor < 0) {
            throw new IllegalArgumentException(que + " no puede ser negativa");
        }
        if (valor > MAXIMO) {
            throw new IllegalArgumentException(que + " no puede pasar de " + MAXIMO + " unidades");
        }
    }

    public ProductoId producto() {
        return producto;
    }

    public int cantidad() {
        return cantidad;
    }
}
