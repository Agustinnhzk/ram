package com.ram.reparaciones;

/** Se intentó un paso que el recorrido no permite (por ejemplo, entregar un equipo sin diagnosticar). */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
