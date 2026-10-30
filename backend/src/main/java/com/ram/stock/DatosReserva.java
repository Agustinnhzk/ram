package com.ram.stock;

import com.ram.catalogo.ProductoId;

import java.time.Instant;
import java.util.UUID;

/** @param sePuedeCubrir si está pendiente y hoy hay stock suficiente para cubrirla */
public record DatosReserva(UUID id, ProductoId producto, String productoNombre, UUID pedido, int numeroPedido,
                           int cantidad, EstadoDeReserva estado, Instant creadaEn, Instant resueltaEn,
                           boolean sePuedeCubrir) {
}
