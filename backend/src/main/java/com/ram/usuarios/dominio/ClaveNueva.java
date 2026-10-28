package com.ram.usuarios.dominio;

import java.nio.charset.StandardCharsets;

/**
 * Una contraseña elegida por el usuario, antes de cifrarla. Vive solo un instante:
 * se valida, se cifra y se descarta. Nunca se guarda.
 *
 * <p>Las reglas:
 * <ul>
 *   <li>Mínimo 8 caracteres.</li>
 *   <li>Máximo 72 <b>bytes</b>: BCrypt ignora todo lo que pase de 72 bytes. Una clave más larga
 *       se aceptaría pero solo contarían sus primeros 72 bytes, sin que nadie se entere.
 *       Son bytes y no caracteres porque una "ñ" o una tilde ocupan 2.</li>
 * </ul>
 */
public record ClaveNueva(String valor) {

    static final int MINIMO_CARACTERES = 8;
    static final int MAXIMO_BYTES = 72;

    public ClaveNueva {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (valor.length() < MINIMO_CARACTERES) {
            throw new IllegalArgumentException("La contraseña tiene que tener al menos " + MINIMO_CARACTERES + " caracteres");
        }
        if (valor.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            throw new IllegalArgumentException("La contraseña es demasiado larga");
        }
    }

    /**
     * Un record imprime todos sus campos en toString(). Sin esto, un log o un error que
     * muestre este objeto dejaría la contraseña escrita en los logs.
     */
    @Override
    public String toString() {
        return "ClaveNueva[****]";
    }
}
