package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.dominio.Cliente;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio en memoria para testear los casos de uso sin base de datos. */
class ClientesEnMemoria implements ClientesRepositorio {

    private final Map<ClienteId, Cliente> clientes = new LinkedHashMap<>();

    @Override
    public void guardar(Cliente cliente) {
        clientes.put(cliente.id(), cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        return Optional.ofNullable(clientes.get(id));
    }

    @Override
    public List<Cliente> buscarPorNombre(String textoNormalizado, int limite) {
        return clientes.values().stream()
                .filter(c -> c.nombre().paraBuscar().contains(textoNormalizado))
                .sorted(Comparator.comparing(c -> c.nombre().valor()))
                .limit(limite)
                .toList();
    }
}
