package com.ram.instalaciones.dominio;

import java.util.Objects;
import java.util.UUID;

/** Un cliente con abono de monitoreo. */
public record Abonado(UUID id, UUID clienteId, String direccion, Long monto, boolean activo) {

    public Abonado {
        Objects.requireNonNull(id);
        Objects.requireNonNull(clienteId, "Falta el cliente");
        direccion = Textos.opcional(direccion, "la dirección", 300);
        if (monto != null && (monto <= 0 || monto > 100_000_000L)) {
            throw new IllegalArgumentException("El abono tiene que ser mayor a cero");
        }
    }
}
