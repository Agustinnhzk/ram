package com.ram.tenants.adaptadores.web;

import com.ram.tenants.TallerId;

import java.util.Optional;

/**
 * Guarda el taller de la request que se está atendiendo.
 *
 * <p>Usa un ThreadLocal: cada request la atiende un hilo, y cada hilo ve solo su valor.
 * Es el mismo mecanismo que usa Spring para la transacción o el usuario logueado.
 * Regla de oro: siempre se limpia al terminar la request (lo hace el filtro en un finally),
 * porque los hilos se reutilizan y la próxima request podría heredar el taller anterior.
 */
final class ContextoTaller {

    private static final ThreadLocal<TallerId> ACTUAL = new ThreadLocal<>();

    private ContextoTaller() {
    }

    static void establecer(TallerId tallerId) {
        ACTUAL.set(tallerId);
    }

    static Optional<TallerId> actual() {
        return Optional.ofNullable(ACTUAL.get());
    }

    static void limpiar() {
        ACTUAL.remove();
    }
}
