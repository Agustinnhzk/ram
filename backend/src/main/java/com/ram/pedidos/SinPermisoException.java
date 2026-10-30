package com.ram.pedidos;

/** Quien lo pide no tiene el rol necesario (por ejemplo, un Técnico confirmando un pago). */
public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
