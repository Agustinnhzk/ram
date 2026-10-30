package com.ram.catalogo;

/** Una fila de la vista previa de un aumento: cómo queda cada precio antes de confirmar. */
public record PrecioPropuesto(ProductoId id, String nombre, String categoria, long precioActual, long precioNuevo) {
}
