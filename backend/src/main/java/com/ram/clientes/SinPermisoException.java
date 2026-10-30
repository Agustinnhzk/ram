package com.ram.clientes;

public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
