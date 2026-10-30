package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.dominio.Cliente;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida. Todas las operaciones son, implícitamente, del taller actual. */
public interface ClientesRepositorio {

    void guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(ClienteId id);

    List<Cliente> buscarVarios(Collection<ClienteId> ids);

    /** @param textoNormalizado ya pasado por {@code NombreCompleto.normalizar} */
    List<Cliente> buscarPorNombre(String textoNormalizado, int limite);

    /** @param digitos solo números: encuentra los teléfonos que los contienen */
    List<Cliente> buscarPorTelefono(String digitos, int limite);

    /** Los últimos cargados, del más nuevo al más viejo. */
    List<Cliente> ultimos(int limite);

    Optional<Cliente> porCuentaDeTienda(UUID cuenta);

    /** Fichas SIN cuenta vinculada con ese correo exacto (ya en minúsculas). */
    List<Cliente> sinCuentaConCorreo(String correo);

    /** Fichas SIN cuenta vinculada con ese teléfono exacto (ya normalizado). */
    List<Cliente> sinCuentaConTelefono(String telefono);

    /** Si la base no lo deja (otra tabla la usa), salta acá mismo, no al final de la transacción. */
    void borrar(ClienteId id);
}
