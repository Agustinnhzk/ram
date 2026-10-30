package com.ram.clientes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Varios clientes en una sola consulta (por ejemplo, para el listado de reparaciones).
     * Los que no existen en este taller simplemente no vienen.
     */
    List<DatosCliente> obtenerVarios(Collection<ClienteId> ids);

    /**
     * Cambia teléfono y correo. El correo puede ser null para quitarlo.
     *
     * @throws ClienteNoEncontradoException si el cliente no existe en este taller
     */
    void actualizarContacto(ClienteId id, String telefono, String correo);

    // ─── Ficha completa (pantalla Clientes del sistema, ADR-014) ───────────────────────────────

    /**
     * Sin texto: los últimos clientes cargados. Con números: busca por teléfono. Si no, por nombre.
     * Hasta 50 resultados. La cuenta de la tienda viene solo con su id.
     */
    List<FichaDeCliente> listar(String texto);

    /** Con la cuenta de la tienda completa (nombre y correo). */
    Optional<FichaDeCliente> ficha(ClienteId id);

    /** @throws ClienteNoEncontradoException si no existe; IllegalArgumentException si un dato es inválido */
    void editar(ClienteId id, DatosDeFicha datos);

    /**
     * Vincula a mano una cuenta de la tienda, buscándola por su correo.
     *
     * @throws IllegalArgumentException     si no hay una cuenta con ese correo
     * @throws CuentaYaVinculadaException   si esa cuenta ya está en otra ficha
     */
    void vincularCuenta(ClienteId id, String correoDeLaCuenta);

    void desvincularCuenta(ClienteId id);

    /** La ficha vinculada a una cuenta de la tienda (para mostrarle sus reparaciones). */
    Optional<ClienteId> deLaCuenta(UUID cuenta);

    /**
     * Alguien hizo un pedido en la tienda: lo reconoce y vincula su cuenta con su ficha (ADR-014).
     * <ol>
     *   <li>Si la cuenta ya tiene ficha, esa.</li>
     *   <li>Si hay UNA ficha sin cuenta con el mismo correo, o (si no) con el mismo teléfono, se vincula esa.</li>
     *   <li>Si no, se crea una ficha nueva con los datos de la cuenta y el teléfono del pedido.</li>
     * </ol>
     * Nunca falla por datos raros (un teléfono que no se puede guardar): en ese caso no vincula y listo.
     */
    Optional<ClienteId> vincularAlComprar(UUID cuenta, String telefono);

    /**
     * Borra la ficha y, si tiene, su cuenta de la tienda (para limpiar las pruebas, ADR-016). Solo el Dueño.
     *
     * @throws SinPermisoException si no es el Dueño
     * @throws org.springframework.dao.DataIntegrityViolationException si tiene reparaciones, instalaciones,
     *         abonos o pedidos: la base no deja borrarla (se borran esos primero)
     */
    void borrar(ClienteId id);
}
