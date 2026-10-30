package com.ram.reparaciones;

public class NumeroDeOrdenRepetidoException extends RuntimeException {

    public NumeroDeOrdenRepetidoException(int numero) {
        super("Ya existe una orden con el número " + numero);
    }
}
