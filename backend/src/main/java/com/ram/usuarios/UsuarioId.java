package com.ram.usuarios;

import java.util.Objects;
import java.util.UUID;

public record UsuarioId(UUID valor) {

    public UsuarioId {
        Objects.requireNonNull(valor, "El id del usuario no puede ser nulo");
    }

    public static UsuarioId nuevo() {
        return new UsuarioId(UUID.randomUUID());
    }

    public static UsuarioId de(UUID valor) {
        return new UsuarioId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
