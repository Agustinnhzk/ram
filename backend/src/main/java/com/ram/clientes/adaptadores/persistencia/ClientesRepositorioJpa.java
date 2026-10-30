package com.ram.clientes.adaptadores.persistencia;

import com.ram.clientes.ClienteId;
import com.ram.clientes.aplicacion.ClientesRepositorio;
import com.ram.clientes.dominio.Cliente;
import com.ram.clientes.dominio.Correo;
import com.ram.clientes.dominio.NombreCompleto;
import com.ram.clientes.dominio.Telefono;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class ClientesRepositorioJpa implements ClientesRepositorio {

    private final ClienteJpaRepository jpa;

    ClientesRepositorioJpa(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    /**
     * Si el cliente ya existe, se actualiza la entidad cargada (que ya trae su taller);
     * si no, se crea una nueva y Hibernate le asigna el taller actual.
     * Nunca se arma una entidad "suelta" con un id existente: así el taller no se pisa.
     */
    @Override
    public void guardar(Cliente cliente) {
        ClienteJpaEntity entidad = jpa.buscarPorId(cliente.id().valor())
                .orElseGet(() -> new ClienteJpaEntity(cliente.id().valor()));
        entidad.actualizar(
                cliente.nombre().valor(),
                cliente.nombre().paraBuscar(),
                cliente.telefono().valor(),
                cliente.correo().map(Correo::valor).orElse(null),
                cliente.notas().orElse(null),
                cliente.cuentaTienda().orElse(null));
        jpa.save(entidad);
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        return jpa.buscarPorId(id.valor()).map(ClientesRepositorioJpa::aDominio);
    }

    @Override
    public List<Cliente> buscarVarios(Collection<ClienteId> ids) {
        return jpa.buscarVarios(ids.stream().map(ClienteId::valor).toList()).stream()
                .map(ClientesRepositorioJpa::aDominio)
                .toList();
    }

    @Override
    public List<Cliente> buscarPorNombre(String textoNormalizado, int limite) {
        return jpa.buscarPorNombre(textoNormalizado, PageRequest.of(0, limite)).stream()
                .map(ClientesRepositorioJpa::aDominio)
                .toList();
    }

    @Override
    public List<Cliente> buscarPorTelefono(String digitos, int limite) {
        return lista(jpa.buscarPorTelefono(digitos, PageRequest.of(0, limite)));
    }

    @Override
    public List<Cliente> ultimos(int limite) {
        return lista(jpa.ultimos(PageRequest.of(0, limite)));
    }

    @Override
    public Optional<Cliente> porCuentaDeTienda(UUID cuenta) {
        return jpa.porCuenta(cuenta).map(ClientesRepositorioJpa::aDominio);
    }

    @Override
    public List<Cliente> sinCuentaConCorreo(String correo) {
        return lista(jpa.sinCuentaConCorreo(correo));
    }

    @Override
    public List<Cliente> sinCuentaConTelefono(String telefono) {
        return lista(jpa.sinCuentaConTelefono(telefono));
    }

    @Override
    public void borrar(ClienteId id) {
        jpa.buscarPorId(id.valor()).ifPresent(jpa::delete);
        jpa.flush();
    }

    private static List<Cliente> lista(List<ClienteJpaEntity> entidades) {
        return entidades.stream().map(ClientesRepositorioJpa::aDominio).toList();
    }

    private static Cliente aDominio(ClienteJpaEntity e) {
        return Cliente.reconstruir(
                ClienteId.de(e.getId()),
                new NombreCompleto(e.getNombreCompleto()),
                new Telefono(e.getTelefono()),
                Correo.opcional(e.getCorreo()),
                e.getNotas(),
                e.getCuentaTiendaId(),
                e.getCreadoEn());
    }
}
