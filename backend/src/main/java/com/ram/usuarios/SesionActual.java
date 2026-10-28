package com.ram.usuarios;

import java.util.Optional;

/**
 * Para que otros módulos sepan quién está haciendo la acción.
 * Por ejemplo, reparaciones va a registrar qué usuario cargó cada ingreso.
 */
public interface SesionActual {

    Optional<UsuarioAutenticado> usuario();

    /** @throws IllegalStateException si no hay nadie logueado */
    default UsuarioAutenticado requerido() {
        return usuario().orElseThrow(() -> new IllegalStateException("No hay un usuario logueado"));
    }
}
