package com.ram.stock.adaptadores.persistencia;

import com.ram.catalogo.ProductoId;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.aplicacion.StockRepositorio;
import com.ram.stock.dominio.Existencia;
import com.ram.stock.dominio.Movimiento;
import com.ram.stock.dominio.Reserva;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class StockRepositorioJpa implements StockRepositorio {

    private final ExistenciaJpaRepository existencias;
    private final MovimientoStockJpaRepository movimientos;
    private final ReservaJpaRepository reservas;
    private final Clock reloj = Clock.systemUTC();

    StockRepositorioJpa(ExistenciaJpaRepository existencias, MovimientoStockJpaRepository movimientos,
                        ReservaJpaRepository reservas) {
        this.existencias = existencias;
        this.movimientos = movimientos;
        this.reservas = reservas;
    }

    @Override
    public Optional<Existencia> paraModificar(ProductoId producto) {
        return existencias.bloquear(producto.valor()).map(e -> Existencia.reconstruir(ProductoId.de(e.productoId()), e.cantidad()));
    }

    /** Si ya está cargada (la trajo paraModificar), Hibernate la tiene en la sesión: se cambia y listo. */
    @Override
    public void guardar(Existencia existencia) {
        ExistenciaJpaEntity entidad = existencias.bloquear(existencia.producto().valor())
                .orElseGet(() -> new ExistenciaJpaEntity(existencia.producto().valor()));
        entidad.actualizar(existencia.cantidad(), reloj.instant());
        existencias.save(entidad);
    }

    /** Se carga y se borra la entidad (no un DELETE masivo): así pasa por el filtro de taller de Hibernate. */
    @Override
    public void borrar(ProductoId producto) {
        existencias.bloquear(producto.valor()).ifPresent(existencias::delete);
    }

    @Override
    public Map<ProductoId, Integer> cantidades() {
        return aMapa(existencias.todas());
    }

    @Override
    public Map<ProductoId, Integer> cantidades(Collection<ProductoId> productos) {
        if (productos.isEmpty()) {
            return Map.of();
        }
        return aMapa(existencias.de(productos.stream().map(ProductoId::valor).toList()));
    }

    @Override
    public void agregar(Movimiento m) {
        movimientos.save(new MovimientoStockJpaEntity(m.id(), m.producto().valor(), m.tipo(), m.cantidad(), m.saldo(),
                m.nota(), m.pedido(), m.numeroPedido(), m.usuario(), m.fecha()));
    }

    @Override
    public List<Movimiento> movimientos(ProductoId producto, int limite) {
        return movimientos.deProducto(producto.valor(), PageRequest.of(0, limite)).stream()
                .map(StockRepositorioJpa::aDominio).toList();
    }

    @Override
    public List<Movimiento> movimientosDePedido(UUID pedido) {
        return movimientos.dePedido(pedido).stream().map(StockRepositorioJpa::aDominio).toList();
    }

    @Override
    public void guardar(Reserva r) {
        ReservaJpaEntity entidad = reservas.buscarPorId(r.id())
                .orElseGet(() -> new ReservaJpaEntity(r.id(), r.producto().valor(), r.pedido(), r.numeroPedido(),
                        r.cantidad(), r.creadaEn()));
        entidad.actualizar(r.estado(), r.resueltaEn());
        reservas.save(entidad);
    }

    @Override
    public Optional<Reserva> reserva(UUID id) {
        return reservas.buscarPorId(id).map(StockRepositorioJpa::aDominio);
    }

    @Override
    public List<Reserva> reservasPendientes() {
        return reservas.conEstado(EstadoDeReserva.PENDIENTE).stream().map(StockRepositorioJpa::aDominio).toList();
    }

    @Override
    public List<Reserva> reservasDePedido(UUID pedido) {
        return reservas.dePedido(pedido).stream().map(StockRepositorioJpa::aDominio).toList();
    }

    private static Map<ProductoId, Integer> aMapa(List<ExistenciaJpaEntity> lista) {
        return lista.stream().collect(Collectors.toMap(e -> ProductoId.de(e.productoId()), ExistenciaJpaEntity::cantidad));
    }

    private static Movimiento aDominio(MovimientoStockJpaEntity e) {
        return new Movimiento(e.id(), ProductoId.de(e.productoId()), e.tipo(), e.cantidad(), e.saldo(), e.nota(),
                e.pedidoId(), e.pedidoNumero(), e.usuario(), e.fecha());
    }

    private static Reserva aDominio(ReservaJpaEntity e) {
        return Reserva.reconstruir(e.id(), ProductoId.de(e.productoId()), e.pedidoId(), e.pedidoNumero(), e.cantidad(),
                e.creadaEn(), e.estado(), e.resueltaEn());
    }
}
