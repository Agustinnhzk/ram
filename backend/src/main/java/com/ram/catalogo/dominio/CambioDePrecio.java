package com.ram.catalogo.dominio;

import com.ram.catalogo.MotivoDeCambio;
import com.ram.catalogo.ProductoId;
import com.ram.usuarios.UsuarioId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Un cambio de precio, tal como queda en el historial. No se modifica nunca: solo se agrega.
 *
 * @param anterior   null si antes era "a consultar"
 * @param nuevo      null si pasa a "a consultar"
 * @param porcentaje obligatorio si el motivo es PORCENTAJE; null si es MANUAL. Es un número simple (no
 *                   {@link Porcentaje}) a propósito: si un día cambian los límites, el historial viejo se sigue leyendo.
 */
public record CambioDePrecio(
        ProductoId producto,
        Pesos anterior,
        Pesos nuevo,
        MotivoDeCambio motivo,
        BigDecimal porcentaje,
        UsuarioId usuario,
        String nombreUsuario,
        Instant cuando) {

    public CambioDePrecio {
        Objects.requireNonNull(producto);
        Objects.requireNonNull(motivo);
        Objects.requireNonNull(usuario);
        Objects.requireNonNull(nombreUsuario);
        Objects.requireNonNull(cuando);
        if ((motivo == MotivoDeCambio.PORCENTAJE) != (porcentaje != null)) {
            throw new IllegalArgumentException("El porcentaje va solo (y siempre) en los cambios por porcentaje");
        }
    }
}
