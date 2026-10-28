package com.ram.clientes;

import java.util.Objects;
import java.util.UUID;

public record ClienteId(UUID valor) {

    public ClienteId {
        Objects.requireNonNull(valor, "El id del cliente no puede ser nulo");
    }

    public static ClienteId nuevo() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(UUID valor) {
        return new ClienteId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
