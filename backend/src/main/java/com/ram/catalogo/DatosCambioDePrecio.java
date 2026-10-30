package com.ram.catalogo;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Una línea del historial de precios de un producto.
 *
 * @param precioAnterior null si antes era "a consultar"
 * @param precioNuevo    null si pasó a "a consultar"
 * @param porcentaje     solo si el motivo es PORCENTAJE
 */
public record DatosCambioDePrecio(
        Long precioAnterior,
        Long precioNuevo,
        MotivoDeCambio motivo,
        BigDecimal porcentaje,
        String usuario,
        Instant cambiadoEn) {
}
