package com.ram.stock.aplicacion;

import com.ram.catalogo.ProductoId;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.dominio.Existencia;
import com.ram.stock.dominio.Movimiento;
import com.ram.stock.dominio.Reserva;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** El repositorio de stock en memoria, para probar el servicio sin base. */
class StockEnMemoria implements StockRepositorio {

    final Map<ProductoId, Integer> existencias = new HashMap<>();
    final List<Movimiento> movimientos = new ArrayList<>();
    final Map<UUID, Reserva> reservas = new LinkedHashMap<>();

    @Override
    public Optional<Existencia> paraModificar(ProductoId producto) {
        return Optional.ofNullable(existencias.get(producto)).map(c -> Existencia.reconstruir(producto, c));
    }

    @Override
    public void guardar(Existencia existencia) {
        existencias.put(existencia.producto(), existencia.cantidad());
    }

    @Override
    public void borrar(ProductoId producto) {
        existencias.remove(producto);
    }

    @Override
    public Map<ProductoId, Integer> cantidades() {
        return Map.copyOf(existencias);
    }

    @Override
    public Map<ProductoId, Integer> cantidades(Collection<ProductoId> productos) {
        Map<ProductoId, Integer> encontradas = new HashMap<>();
        productos.forEach(p -> {
            if (existencias.containsKey(p)) {
                encontradas.put(p, existencias.get(p));
            }
        });
        return encontradas;
    }

    @Override
    public void agregar(Movimiento movimiento) {
        movimientos.add(movimiento);
    }

    @Override
    public List<Movimiento> movimientos(ProductoId producto, int limite) {
        List<Movimiento> delProducto = new ArrayList<>(movimientos.stream().filter(m -> m.producto().equals(producto)).toList());
        java.util.Collections.reverse(delProducto);
        return delProducto.stream().limit(limite).toList();
    }

    @Override
    public List<Movimiento> movimientosDePedido(UUID pedido) {
        return movimientos.stream().filter(m -> pedido.equals(m.pedido())).toList();
    }

    @Override
    public void guardar(Reserva reserva) {
        reservas.put(reserva.id(), reserva);
    }

    @Override
    public Optional<Reserva> reserva(UUID id) {
        return Optional.ofNullable(reservas.get(id));
    }

    @Override
    public List<Reserva> reservasPendientes() {
        return reservas.values().stream().filter(r -> r.estado() == EstadoDeReserva.PENDIENTE)
                .sorted(Comparator.comparing(Reserva::creadaEn)).toList();
    }

    @Override
    public List<Reserva> reservasDePedido(UUID pedido) {
        return reservas.values().stream().filter(r -> r.pedido().equals(pedido)).toList();
    }
}
