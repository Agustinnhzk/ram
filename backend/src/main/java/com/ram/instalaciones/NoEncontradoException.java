package com.ram.instalaciones;

public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException() {
        super("No existe");
    }
}
