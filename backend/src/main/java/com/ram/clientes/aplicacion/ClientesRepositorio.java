package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.dominio.Cliente;

import java.util.List;
import java.util.Optional;

/** Puerto de salida. Todas las operaciones son, implícitamente, del taller actual. */
public interface ClientesRepositorio {

    void guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(ClienteId id);

    /** @param textoNormalizado ya pasado por {@code NombreCompleto.normalizar} */
    List<Cliente> buscarPorNombre(String textoNormalizado, int limite);
}
