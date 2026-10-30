package com.ram.pedidos;

/** No existe, es de otro taller, o (desde la tienda) es de otro cliente: la respuesta es la misma. */
public class PedidoNoEncontradoException extends RuntimeException {

    public PedidoNoEncontradoException(PedidoId id) {
        super("No existe el pedido " + id);
    }
}
