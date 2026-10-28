package com.ram.tenants.adaptadores.contexto;

import com.ram.tenants.TallerId;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Guarda el taller de la request que se está atendiendo.
 *
 * <p>Usa un ThreadLocal: cada request la atiende un hilo, y cada hilo ve solo su valor.
 * Es el mismo mecanismo que usa Spring para la transacción o el usuario logueado.
 * Regla de oro: siempre se limpia al terminar (lo hace el filtro en un finally),
 * porque los hilos se reutilizan y la próxima request podría heredar el taller anterior.
 *
 * <p>Es público para que lo usen el filtro web y la persistencia, pero sigue siendo interno
 * del módulo tenants: Spring Modulith no deja que otro módulo lo importe.
 */
public final class ContextoTaller {

    private static final ThreadLocal<TallerId> ACTUAL = new ThreadLocal<>();

    private ContextoTaller() {
    }

    public static void establecer(TallerId tallerId) {
        ACTUAL.set(tallerId);
    }

    public static Optional<TallerId> actual() {
        return Optional.ofNullable(ACTUAL.get());
    }

    public static void limpiar() {
        ACTUAL.remove();
    }

    /** Ejecuta algo como si la request fuera de ese taller, y limpia al terminar. Útil en tests. */
    public static <T> T con(TallerId tallerId, Supplier<T> accion) {
        TallerId anterior = ACTUAL.get();
        ACTUAL.set(tallerId);
        try {
            return accion.get();
        } finally {
            if (anterior == null) {
                ACTUAL.remove();
            } else {
                ACTUAL.set(anterior);
            }
        }
    }
}
