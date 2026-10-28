package com.ram.clientes;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada del módulo: todo lo que el resto del sistema puede hacer con clientes.
 *
 * <p>Siempre trabaja sobre el taller de la request actual. No hay forma de pedir
 * los clientes de otro taller, ni siquiera por error.
 */
public interface Clientes {

    /**
     * Da de alta un cliente.
     *
     * @throws IllegalArgumentException si algún dato es inválido (nombre vacío, teléfono o correo mal escritos)
     */
    ClienteId registrar(NuevoCliente nuevo);

    /**
     * Busca clientes cuyo nombre contenga el texto, sin importar mayúsculas, tildes ni espacios de más.
     * "agustin perez" encuentra a "Agustín  Pérez". Devuelve hasta 20 resultados, ordenados por nombre.
     */
    List<DatosCliente> buscarPorNombre(String texto);

    Optional<DatosCliente> obtener(ClienteId id);

    /**
     * Cambia teléfono y correo. El correo puede ser null para quitarlo.
     *
     * @throws ClienteNoEncontradoException si el cliente no existe en este taller
     */
    void actualizarContacto(ClienteId id, String telefono, String correo);
}
