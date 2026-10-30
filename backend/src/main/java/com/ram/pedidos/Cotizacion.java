package com.ram.pedidos;

import java.util.List;

/**
 * Cuánto saldría un pedido con los precios de ahora, sin guardarlo: lo que muestra el checkout antes de confirmar.
 *
 * @param envioACotizar true si el envío es fuera de Rosario: se cotiza por WhatsApp y no está en el total
 */
public record Cotizacion(List<LineaDePedido> lineas, long total, boolean envioACotizar) {
}
