package com.ram.pedidos.adaptadores.config;

import com.ram.archivos.ArchivoSubido;
import com.ram.pedidos.ArchivoDeComprobante;
import com.ram.pedidos.Cotizacion;
import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.DetalleDePedido;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.NuevoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.Pedidos;
import com.ram.pedidos.ResumenDePedido;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Decorador con transacciones, como en los otros módulos. Lo importante: crear el pedido, guardar el comprobante
 * y anotar el evento del mail pasan juntos o no pasa nada.
 */
class PedidosTransaccional implements Pedidos {

    private final Pedidos delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    PedidosTransaccional(Pedidos delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public Optional<DatosDePago> datosDePago() {
        return lectura.execute(estado -> delegado.datosDePago());
    }

    @Override
    public Optional<Long> precioDelArmado() {
        return lectura.execute(estado -> delegado.precioDelArmado());
    }

    @Override
    public void cambiarPrecioDelArmado(Long precio) {
        escritura.executeWithoutResult(estado -> delegado.cambiarPrecioDelArmado(precio));
    }

    @Override
    public Cotizacion cotizar(NuevoPedido pedido) {
        return lectura.execute(estado -> delegado.cotizar(pedido));
    }

    @Override
    public DetalleDePedido realizar(NuevoPedido pedido) {
        return escritura.execute(estado -> delegado.realizar(pedido));
    }

    @Override
    public List<ResumenDePedido> misPedidos() {
        return lectura.execute(estado -> delegado.misPedidos());
    }

    @Override
    public Optional<DetalleDePedido> miPedido(PedidoId id) {
        return lectura.execute(estado -> delegado.miPedido(id));
    }

    @Override
    public void informarPago(PedidoId id, ArchivoSubido comprobante) {
        escritura.executeWithoutResult(estado -> delegado.informarPago(id, comprobante));
    }

    @Override
    public void cancelarMiPedido(PedidoId id) {
        escritura.executeWithoutResult(estado -> delegado.cancelarMiPedido(id));
    }

    @Override
    public List<ResumenDePedido> listar(EstadoPedido estado) {
        return lectura.execute(e -> delegado.listar(estado));
    }

    @Override
    public List<ResumenDePedido> deCuenta(UUID cuenta) {
        return lectura.execute(e -> delegado.deCuenta(cuenta));
    }

    @Override
    public void borrar(PedidoId id) {
        escritura.executeWithoutResult(e -> delegado.borrar(id));
    }

    @Override
    public Optional<DetalleDePedido> obtener(PedidoId id) {
        return lectura.execute(estado -> delegado.obtener(id));
    }

    @Override
    public Optional<ArchivoDeComprobante> comprobante(PedidoId pedido, UUID comprobante) {
        return lectura.execute(estado -> delegado.comprobante(pedido, comprobante));
    }

    @Override
    public void confirmarPago(PedidoId id) {
        escritura.executeWithoutResult(estado -> delegado.confirmarPago(id));
    }

    @Override
    public void rechazarComprobante(PedidoId id, String motivo) {
        escritura.executeWithoutResult(estado -> delegado.rechazarComprobante(id, motivo));
    }

    @Override
    public void entregar(PedidoId id) {
        escritura.executeWithoutResult(estado -> delegado.entregar(id));
    }

    @Override
    public void cancelar(PedidoId id, String motivo) {
        escritura.executeWithoutResult(estado -> delegado.cancelar(id, motivo));
    }
}
