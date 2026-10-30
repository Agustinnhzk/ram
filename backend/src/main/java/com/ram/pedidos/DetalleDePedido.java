package com.ram.pedidos;

import java.time.Instant;
import java.util.List;

/**
 * Todo el pedido. Lo ven el cliente (el suyo) y el personal.
 *
 * @param datosDePago a dónde transferir (null si el taller no los cargó)
 * @param puedeSubirComprobante si el cliente puede subir (o volver a subir) un comprobante ahora
 * @param puedeCancelar         si el cliente puede cancelarlo él mismo ahora
 */
public record DetalleDePedido(
        PedidoId id,
        int numero,
        String cliente,
        String telefono,
        FormaDeEntrega entrega,
        String direccion,
        String nota,
        List<LineaDePedido> lineas,
        long total,
        EstadoPedido estado,
        Instant creadoEn,
        Instant pagadoEn,
        Instant entregadoEn,
        Instant canceladoEn,
        String motivoCancelacion,
        List<DatosComprobante> comprobantes,
        DatosDePago datosDePago,
        boolean puedeSubirComprobante,
        boolean puedeCancelar) {
}
