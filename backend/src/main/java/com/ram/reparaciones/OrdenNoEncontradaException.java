package com.ram.reparaciones;

public class OrdenNoEncontradaException extends RuntimeException {

    public OrdenNoEncontradaException(OrdenId id) {
        super("No existe la orden " + id);
    }
}
