package com.ram.clientes;

import java.util.Optional;

/**
 * Lo que otros módulos ven de un cliente. Es una foto de solo lectura:
 * para cambiar algo hay que pasar por {@link Clientes}.
 *
 * @param correo null si el cliente no tiene; usar {@link #correoOpcional()}
 */
public record DatosCliente(ClienteId id, String nombreCompleto, String telefono, String correo) {

    public Optional<String> correoOpcional() {
        return Optional.ofNullable(correo);
    }
}
