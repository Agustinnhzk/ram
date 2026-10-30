package com.ram.clientes.adaptadores.config;

import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Envuelve los casos de uso en transacciones, sin poner anotaciones de Spring en la capa de aplicación.
 *
 * <p>Es el patrón "decorador": implementa la misma interfaz, delega todo y agrega solo la transacción.
 * Por ejemplo, actualizarContacto lee y guarda: los dos pasos quedan en una sola transacción.
 */
class ClientesTransaccionales implements Clientes {

    private final Clientes delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    ClientesTransaccionales(Clientes delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public ClienteId registrar(NuevoCliente nuevo) {
        return escritura.execute(estado -> delegado.registrar(nuevo));
    }

    @Override
    public List<DatosCliente> buscarPorNombre(String texto) {
        return lectura.execute(estado -> delegado.buscarPorNombre(texto));
    }

    @Override
    public Optional<DatosCliente> obtener(ClienteId id) {
        return lectura.execute(estado -> delegado.obtener(id));
    }

    @Override
    public List<DatosCliente> obtenerVarios(Collection<ClienteId> ids) {
        return lectura.execute(estado -> delegado.obtenerVarios(ids));
    }

    @Override
    public void actualizarContacto(ClienteId id, String telefono, String correo) {
        escritura.executeWithoutResult(estado -> delegado.actualizarContacto(id, telefono, correo));
    }

    @Override
    public List<FichaDeCliente> listar(String texto) {
        return lectura.execute(estado -> delegado.listar(texto));
    }

    @Override
    public Optional<FichaDeCliente> ficha(ClienteId id) {
        return lectura.execute(estado -> delegado.ficha(id));
    }

    @Override
    public void editar(ClienteId id, DatosDeFicha datos) {
        escritura.executeWithoutResult(estado -> delegado.editar(id, datos));
    }

    @Override
    public void vincularCuenta(ClienteId id, String correoDeLaCuenta) {
        escritura.executeWithoutResult(estado -> delegado.vincularCuenta(id, correoDeLaCuenta));
    }

    @Override
    public void desvincularCuenta(ClienteId id) {
        escritura.executeWithoutResult(estado -> delegado.desvincularCuenta(id));
    }

    @Override
    public Optional<ClienteId> deLaCuenta(UUID cuenta) {
        return lectura.execute(estado -> delegado.deLaCuenta(cuenta));
    }

    /** Dentro de la transacción del pedido (REQUIRED): la ficha y el pedido se guardan juntos. */
    @Override
    public Optional<ClienteId> vincularAlComprar(UUID cuenta, String telefono) {
        return escritura.execute(estado -> delegado.vincularAlComprar(cuenta, telefono));
    }

    @Override
    public void borrar(ClienteId id) {
        escritura.executeWithoutResult(estado -> delegado.borrar(id));
    }
}
