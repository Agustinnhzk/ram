package com.ram.tenants;

import java.util.Optional;

/**
 * Para trabajar "como si fuera" un taller fuera de una request web: el alta inicial
 * del dueño al arrancar, y más adelante tareas programadas (por ejemplo, vencer garantías).
 *
 * <p>Dentro de una request no hace falta: el filtro web ya dejó el taller resuelto.
 */
public interface ContextoDeTaller {

    Optional<TallerId> buscarPorSlug(String slug);

    /** Ejecuta la acción con ese taller como taller actual, y lo limpia al terminar. */
    void ejecutarComo(TallerId taller, Runnable accion);
}
