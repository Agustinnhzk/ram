package com.ram.reparaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.dominio.OrdenDeReparacion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

/** Versiones en memoria de lo que necesita el servicio, para testearlo sin base de datos. */
final class DoblesDeReparaciones {

    private DoblesDeReparaciones() {
    }

    static final class OrdenesEnMemoria implements OrdenesRepositorio {

        private final Map<OrdenId, OrdenDeReparacion> ordenes = new LinkedHashMap<>();

        @Override
        public void guardar(OrdenDeReparacion orden) {
            ordenes.put(orden.id(), orden);
        }

        @Override
        public Optional<OrdenDeReparacion> porId(OrdenId id) {
            return Optional.ofNullable(ordenes.get(id));
        }

        @Override
        public Optional<OrdenDeReparacion> porNumero(int numero) {
            return ordenes.values().stream().filter(o -> o.numero() == numero).findFirst();
        }

        @Override
        public boolean existeNumero(int numero) {
            return porNumero(numero).isPresent();
        }

        @Override
        public OptionalInt ultimoNumero() {
            return ordenes.values().stream().mapToInt(OrdenDeReparacion::numero).max();
        }

        @Override
        public List<OrdenDeReparacion> conEstados(Set<EstadoReparacion> estados, int limite) {
            return ordenes.values().stream().filter(o -> estados.contains(o.estado()))
                    .sorted(Comparator.comparingInt(OrdenDeReparacion::numero).reversed()).limit(limite).toList();
        }

        @Override
        public List<OrdenDeReparacion> deClientes(Collection<ClienteId> clientes, int limite) {
            return ordenes.values().stream().filter(o -> clientes.contains(o.cliente()))
                    .sorted(Comparator.comparingInt(OrdenDeReparacion::numero).reversed()).limit(limite).toList();
        }
    }

    /** Clientes mínimo: guarda el teléfono solo con dígitos, como el módulo de verdad. */
    static final class ClientesEnMemoria implements Clientes {

        private final Map<ClienteId, DatosCliente> clientes = new LinkedHashMap<>();

        @Override
        public ClienteId registrar(NuevoCliente nuevo) {
            if (nuevo.nombreCompleto() == null || nuevo.nombreCompleto().isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            ClienteId id = ClienteId.nuevo();
            clientes.put(id, new DatosCliente(id, nuevo.nombreCompleto(), nuevo.telefono().replaceAll("\\D", ""), nuevo.correo()));
            return id;
        }

        @Override
        public List<DatosCliente> buscarPorNombre(String texto) {
            String buscado = texto.toLowerCase();
            return clientes.values().stream().filter(c -> c.nombreCompleto().toLowerCase().contains(buscado)).toList();
        }

        @Override
        public Optional<DatosCliente> obtener(ClienteId id) {
            return Optional.ofNullable(clientes.get(id));
        }

        @Override
        public List<DatosCliente> obtenerVarios(Collection<ClienteId> ids) {
            return ids.stream().map(clientes::get).filter(Objects::nonNull).toList();
        }

        @Override
        public void actualizarContacto(ClienteId id, String telefono, String correo) {
            throw new ClienteNoEncontradoException(id);
        }

        /** Cuenta de la tienda → ficha (para "mis reparaciones"). */
        final Map<UUID, ClienteId> cuentas = new HashMap<>();

        @Override
        public Optional<ClienteId> deLaCuenta(UUID cuenta) {
            return Optional.ofNullable(cuentas.get(cuenta));
        }

        @Override
        public List<FichaDeCliente> listar(String texto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<FichaDeCliente> ficha(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void editar(ClienteId id, DatosDeFicha datos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void vincularCuenta(ClienteId id, String correoDeLaCuenta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void desvincularCuenta(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ClienteId> vincularAlComprar(UUID cuenta, String telefono) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void borrar(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        int cantidad() {
            return clientes.size();
        }
    }
}
