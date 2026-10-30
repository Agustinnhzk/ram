package com.ram.pedidos;

import java.util.Objects;
import java.util.UUID;

public record PedidoId(UUID valor) {

    public PedidoId {
        Objects.requireNonNull(valor);
    }

    public static PedidoId nuevo() {
        return new PedidoId(UUID.randomUUID());
    }

    public static PedidoId de(UUID valor) {
        return new PedidoId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
