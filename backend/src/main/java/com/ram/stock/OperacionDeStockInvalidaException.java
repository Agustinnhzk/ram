package com.ram.stock;

/** Lo que se pidió choca con el estado actual (ya se controla, no hay suficiente, hay reservas...). Responde 409. */
public class OperacionDeStockInvalidaException extends RuntimeException {

    public OperacionDeStockInvalidaException(String mensaje) {
        super(mensaje);
    }
}
