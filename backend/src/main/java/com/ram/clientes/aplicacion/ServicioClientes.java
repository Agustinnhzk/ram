package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.clientes.dominio.Cliente;
import com.ram.clientes.dominio.Correo;
import com.ram.clientes.dominio.NombreCompleto;
import com.ram.clientes.dominio.Telefono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Casos de uso de clientes. Implementa la API pública del módulo.
 *
 * <p>Convierte lo que llega de afuera (Strings del formulario) en objetos del dominio,
 * que se validan solos, y devuelve {@link DatosCliente}: nunca expone el objeto de dominio.
 */
public class ServicioClientes implements Clientes {

    /** Suficiente para un autocompletado; si hay más, el usuario sigue escribiendo. */
    static final int MAXIMO_RESULTADOS = 20;

    /** Con menos de 2 caracteres la búsqueda devolvería medio padrón de clientes. */
    static final int MINIMO_CARACTERES_BUSQUEDA = 2;

    private final ClientesRepositorio repositorio;

    public ServicioClientes(ClientesRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    @Override
    public ClienteId registrar(NuevoCliente nuevo) {
        Cliente cliente = Cliente.registrar(
                new NombreCompleto(nuevo.nombreCompleto()),
                new Telefono(nuevo.telefono()),
                Correo.opcional(nuevo.correo()));
        repositorio.guardar(cliente);
        return cliente.id();
    }

    @Override
    public List<DatosCliente> buscarPorNombre(String texto) {
        String normalizado = NombreCompleto.normalizar(texto);
        if (normalizado.length() < MINIMO_CARACTERES_BUSQUEDA) {
            return List.of();
        }
        return repositorio.buscarPorNombre(normalizado, MAXIMO_RESULTADOS).stream()
                .map(ServicioClientes::aDatos)
                .toList();
    }

    @Override
    public Optional<DatosCliente> obtener(ClienteId id) {
        return repositorio.buscarPorId(id).map(ServicioClientes::aDatos);
    }

    @Override
    public void actualizarContacto(ClienteId id, String telefono, String correo) {
        Cliente cliente = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
        cliente.actualizarContacto(new Telefono(telefono), Correo.opcional(correo));
        repositorio.guardar(cliente);
    }

    private static DatosCliente aDatos(Cliente c) {
        return new DatosCliente(
                c.id(),
                c.nombre().valor(),
                c.telefono().valor(),
                c.correo().map(Correo::valor).orElse(null));
    }
}
