package com.ram.tenants;

/**
 * Lo único que el resto de los módulos necesita saber de este módulo:
 * en qué taller se está atendiendo la request actual.
 *
 * <p>Los otros módulos inyectan esta interfaz y nunca tocan cómo se resolvió
 * el taller (dominio, subdominio, contexto del hilo). Eso queda adentro de tenants.
 */
public interface TallerActual {

    /**
     * El taller de la request actual.
     *
     * @throws IllegalStateException si se llama fuera de una request asociada a un taller
     */
    TallerId id();

    /** Indica si la request actual tiene un taller asociado. */
    boolean hayTaller();
}
