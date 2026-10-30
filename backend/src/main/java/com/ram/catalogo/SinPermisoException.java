package com.ram.catalogo;

/** El usuario logueado no tiene el rol necesario (por ejemplo, un técnico intentando cambiar precios). */
public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
