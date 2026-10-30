package com.ram.pedidos;

/**
 * Algo del carrito ya no se puede comprar: no existe más, no está publicado o no tiene precio.
 * El mensaje se le muestra al cliente.
 */
public class ProductoNoDisponibleException extends RuntimeException {

    public ProductoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
