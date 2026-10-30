package com.ram.catalogo;

import java.util.Objects;
import java.util.UUID;

public record ProductoId(UUID valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "El id del producto no puede ser nulo");
    }

    public static ProductoId nuevo() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(UUID valor) {
        return new ProductoId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
