package com.ram.pedidos;

/**
 * El pedido no está en un estado que permita eso (entregar uno sin pagar, subir un comprobante a uno pagado...).
 * El mensaje se le puede mostrar a quien lo intentó.
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
