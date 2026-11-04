package com.ram.stock.adaptadores.config;

import com.ram.catalogo.ProductoId;
import com.ram.stock.DatosMovimiento;
import com.ram.stock.DatosReserva;
import com.ram.stock.DisponibilidadPublica;
import com.ram.stock.FilaDeStock;
import com.ram.stock.Stock;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class StockTransaccional implements Stock {

    private final Stock delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    StockTransaccional(Stock delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public List<DisponibilidadPublica> paraLaTienda() {
        return lectura.execute(e -> delegado.paraLaTienda());
    }

    @Override
    public Map<ProductoId, Integer> disponibles(Collection<ProductoId> productos) {
        return lectura.execute(e -> delegado.disponibles(productos));
    }

    @Override
    public void descontarPorVenta(UUID pedido, int numeroDePedido, Map<ProductoId, Integer> unidades) {
        escritura.executeWithoutResult(e -> delegado.descontarPorVenta(pedido, numeroDePedido, unidades));
    }

    @Override
    public void devolverPorCancelacion(UUID pedido, int numeroDePedido) {
        escritura.executeWithoutResult(e -> delegado.devolverPorCancelacion(pedido, numeroDePedido));
    }

    @Override
    public List<FilaDeStock> listar() {
        return lectura.execute(e -> delegado.listar());
    }

    @Override
    public void controlar(ProductoId producto, int cantidadInicial) {
        escritura.executeWithoutResult(e -> delegado.controlar(producto, cantidadInicial));
    }

    @Override
    public void ingresar(ProductoId producto, int cantidad, String nota) {
        escritura.executeWithoutResult(e -> delegado.ingresar(producto, cantidad, nota));
    }

    @Override
    public void ajustar(ProductoId producto, int cantidadContada, String motivo) {
        escritura.executeWithoutResult(e -> delegado.ajustar(producto, cantidadContada, motivo));
    }

    @Override
    public void dejarDeControlar(ProductoId producto) {
        escritura.executeWithoutResult(e -> delegado.dejarDeControlar(producto));
    }

    @Override
    public List<DatosMovimiento> movimientos(ProductoId producto) {
        return lectura.execute(e -> delegado.movimientos(producto));
    }

    @Override
    public List<DatosReserva> reservasPendientes() {
        return lectura.execute(e -> delegado.reservasPendientes());
    }

    @Override
    public List<DatosReserva> reservasDePedido(UUID pedido) {
        return lectura.execute(e -> delegado.reservasDePedido(pedido));
    }

    @Override
    public void cubrirReserva(UUID reserva) {
        escritura.executeWithoutResult(e -> delegado.cubrirReserva(reserva));
    }
}
