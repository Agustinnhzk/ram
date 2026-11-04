package com.ram.reparaciones.adaptadores.config;

import com.ram.clientes.ClienteId;
import com.ram.reparaciones.DetalleDeOrden;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.NuevoIngreso;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.ResumenDeOrden;
import com.ram.reparaciones.Seguimiento;
import com.ram.reparaciones.VistaDeOrdenes;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Decorador con transacciones, como en los otros módulos. Importante: el ingreso registra el cliente
 * nuevo (módulo clientes) y la orden en la MISMA transacción. Si la orden falla, el cliente tampoco queda.
 */
class ReparacionesTransaccional implements Reparaciones {

    private final Reparaciones delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    ReparacionesTransaccional(Reparaciones delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public OptionalInt ultimoNumero() {
        return lectura.execute(estado -> delegado.ultimoNumero());
    }

    @Override
    public OrdenId registrarIngreso(NuevoIngreso ingreso) {
        return escritura.execute(estado -> delegado.registrarIngreso(ingreso));
    }

    @Override
    public List<ResumenDeOrden> listar(VistaDeOrdenes vista, String texto) {
        return lectura.execute(estado -> delegado.listar(vista, texto));
    }

    @Override
    public List<ResumenDeOrden> deCliente(ClienteId cliente) {
        return lectura.execute(estado -> delegado.deCliente(cliente));
    }

    @Override
    public List<Seguimiento> misReparaciones() {
        return lectura.execute(estado -> delegado.misReparaciones());
    }

    @Override
    public Optional<DetalleDeOrden> obtener(OrdenId id) {
        return lectura.execute(estado -> delegado.obtener(id));
    }

    @Override
    public void cambiarEstado(OrdenId id, EstadoReparacion destino, String nota, boolean notaParaCliente) {
        escritura.executeWithoutResult(estado -> delegado.cambiarEstado(id, destino, nota, notaParaCliente));
    }

    @Override
    public void presupuestar(OrdenId id, long monto, String descripcion) {
        escritura.executeWithoutResult(estado -> delegado.presupuestar(id, monto, descripcion));
    }

    @Override
    public void entregar(OrdenId id, int garantiaDias, String nota) {
        escritura.executeWithoutResult(estado -> delegado.entregar(id, garantiaDias, nota));
    }

    @Override
    public void agregarNota(OrdenId id, String texto, boolean paraCliente) {
        escritura.executeWithoutResult(estado -> delegado.agregarNota(id, texto, paraCliente));
    }

    @Override
    public Optional<Seguimiento> seguimiento(int numero, String ultimosDigitosDelTelefono) {
        return lectura.execute(estado -> delegado.seguimiento(numero, ultimosDigitosDelTelefono));
    }
}
