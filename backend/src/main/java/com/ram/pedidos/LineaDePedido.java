package com.ram.pedidos;

import java.util.List;

/**
 * @param referencia a dónde lleva en la tienda: /productos/slug o /arma-tu-pc?...
 * @param reserva    si al pedirla faltaba stock: se cobra igual y se entrega cuando llega (ADR-014)
 */
public record LineaDePedido(String descripcion, List<String> detalle, String referencia, int cantidad,
                            long precioUnitario, long subtotal, boolean reserva) {
}
