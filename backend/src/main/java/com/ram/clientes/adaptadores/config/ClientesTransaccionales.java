package com.ram.clientes.adaptadores.config;

import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

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
    public void actualizarContacto(ClienteId id, String telefono, String correo) {
        escritura.executeWithoutResult(estado -> delegado.actualizarContacto(id, telefono, correo));
    }
}
