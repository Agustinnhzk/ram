package com.ram.trabajos;

public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
