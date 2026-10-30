package com.ram.pedidos;

import java.util.List;

/**
 * Evento: un cliente confirmó un pedido. Lo escucha el aviso por mail al taller.
 * Lleva todo lo que el mail necesita: quien lo escucha no vuelve a consultar la base.
 */
public record PedidoRealizado(int numero, String cliente, String telefono, FormaDeEntrega entrega, String direccion,
                              String nota, List<String> lineas, long total) {
}
