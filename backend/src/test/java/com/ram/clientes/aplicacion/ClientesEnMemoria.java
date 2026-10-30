package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.dominio.Cliente;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public List<Cliente> buscarVarios(Collection<ClienteId> ids) {
        return ids.stream().map(clientes::get).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    public List<Cliente> buscarPorNombre(String textoNormalizado, int limite) {
        return clientes.values().stream()
                .filter(c -> c.nombre().paraBuscar().contains(textoNormalizado))
                .sorted(Comparator.comparing(c -> c.nombre().valor()))
                .limit(limite)
                .toList();
    }

    @Override
    public List<Cliente> buscarPorTelefono(String digitos, int limite) {
        return clientes.values().stream().filter(c -> c.telefono().valor().contains(digitos)).limit(limite).toList();
    }

    @Override
    public List<Cliente> ultimos(int limite) {
        List<Cliente> todos = new java.util.ArrayList<>(clientes.values());
        java.util.Collections.reverse(todos);
        return todos.stream().limit(limite).toList();
    }

    @Override
    public Optional<Cliente> porCuentaDeTienda(UUID cuenta) {
        return clientes.values().stream().filter(c -> c.cuentaTienda().filter(cuenta::equals).isPresent()).findFirst();
    }

    @Override
    public List<Cliente> sinCuentaConCorreo(String correo) {
        return clientes.values().stream()
                .filter(c -> c.cuentaTienda().isEmpty() && c.correo().filter(x -> x.valor().equals(correo)).isPresent()).toList();
    }

    @Override
    public List<Cliente> sinCuentaConTelefono(String telefono) {
        return clientes.values().stream()
                .filter(c -> c.cuentaTienda().isEmpty() && c.telefono().valor().equals(telefono)).toList();
    }

    @Override
    public void borrar(ClienteId id) {
        clientes.remove(id);
    }
}
