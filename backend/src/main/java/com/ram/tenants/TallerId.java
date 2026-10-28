package com.ram.tenants;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un taller.
 *
 * <p>Es un tipo propio en vez de un UUID suelto para que el compilador no deje
 * confundir el id de un taller con el de un cliente o una orden.
 */
public record TallerId(UUID valor) {

    public TallerId {
        Objects.requireNonNull(valor, "El id del taller no puede ser nulo");
    }

    public static TallerId nuevo() {
        return new TallerId(UUID.randomUUID());
    }

    public static TallerId de(UUID valor) {
        return new TallerId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
