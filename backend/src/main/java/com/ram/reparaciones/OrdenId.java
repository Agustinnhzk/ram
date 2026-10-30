package com.ram.reparaciones;

import java.util.Objects;
import java.util.UUID;

public record OrdenId(UUID valor) {

    public OrdenId {
        Objects.requireNonNull(valor, "El id de la orden no puede ser nulo");
    }

    public static OrdenId nueva() {
        return new OrdenId(UUID.randomUUID());
    }

    public static OrdenId de(UUID valor) {
        return new OrdenId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
