package com.ram.tenants.aplicacion;

public class TallerNoEncontradoException extends RuntimeException {

    public TallerNoEncontradoException(String host) {
        super("No hay ningún taller para la dirección '" + host + "'");
    }
}
