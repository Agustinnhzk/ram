package com.ram.reparaciones.dominio;

import com.ram.reparaciones.EstadoReparacion;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Una línea de la bitácora: un cambio de estado (estado != null) o una nota suelta (estado == null).
 * No se modifica nunca.
 */
public record Movimiento(UUID id, EstadoReparacion estado, String nota, boolean paraCliente, Autor autor, Instant cuando) {

    public Movimiento {
        Objects.requireNonNull(id);
        Objects.requireNonNull(autor);
        Objects.requireNonNull(cuando);
        if (estado == null && nota == null) {
            throw new IllegalArgumentException("Un movimiento sin cambio de estado tiene que tener una nota");
        }
    }

    static Movimiento nuevo(EstadoReparacion estado, String nota, boolean paraCliente, Autor autor, Instant cuando) {
        return new Movimiento(UUID.randomUUID(), estado, nota, paraCliente, autor, cuando);
    }
}
