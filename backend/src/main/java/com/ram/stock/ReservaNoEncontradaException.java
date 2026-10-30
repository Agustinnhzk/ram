package com.ram.stock;

public class ReservaNoEncontradaException extends RuntimeException {

    public ReservaNoEncontradaException() {
        super("No existe esa reserva");
    }
}
