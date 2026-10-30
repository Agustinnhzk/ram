package com.ram.pedidos;

import java.time.Instant;

/** Una fila del listado (del sistema o de "Mis pedidos"). */
public record ResumenDePedido(PedidoId id, int numero, String cliente, String telefono, FormaDeEntrega entrega,
                              long total, EstadoPedido estado, Instant creadoEn) {
}
