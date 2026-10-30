package com.ram.stock.aplicacion;

import com.ram.catalogo.ProductoId;
import com.ram.stock.dominio.Existencia;
import com.ram.stock.dominio.Movimiento;
import com.ram.stock.dominio.Reserva;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida. Todo es del taller actual (ADR-005). */
public interface StockRepositorio {

    /**
     * La existencia, <b>bloqueada</b> hasta que termine la transacción: si dos personas confirman el pago de dos pedidos
     * con el último producto al mismo tiempo, la segunda espera a la primera y ve el stock ya descontado.
     */
    Optional<Existencia> paraModificar(ProductoId producto);

    /** Crea o actualiza. */
    void guardar(Existencia existencia);

    void borrar(ProductoId producto);

    /** La cantidad de cada producto con control de stock. */
    Map<ProductoId, Integer> cantidades();

    Map<ProductoId, Integer> cantidades(Collection<ProductoId> productos);

    void agregar(Movimiento movimiento);

    /** Del más nuevo al más viejo. */
    List<Movimiento> movimientos(ProductoId producto, int limite);

    List<Movimiento> movimientosDePedido(UUID pedido);

    /** Crea o actualiza. */
    void guardar(Reserva reserva);

    Optional<Reserva> reserva(UUID id);

    /** De la más vieja a la más nueva. */
    List<Reserva> reservasPendientes();

    List<Reserva> reservasDePedido(UUID pedido);
}
