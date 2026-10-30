package com.ram.stock.dominio;

import com.ram.catalogo.ProductoId;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.OperacionDeStockInvalidaException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Unidades pagadas que no había en stock. Esperan mercadería: el taller las cubre a mano cuando llega
 * (elige a qué pedido le da las unidades), o se cancelan si se cancela el pedido.
 */
public final class Reserva {

    private final UUID id;
    private final ProductoId producto;
    private final UUID pedido;
    private final int numeroPedido;
    private final int cantidad;
    private final Instant creadaEn;
    private EstadoDeReserva estado;
    private Instant resueltaEn;

    private Reserva(UUID id, ProductoId producto, UUID pedido, int numeroPedido, int cantidad, Instant creadaEn,
                    EstadoDeReserva estado, Instant resueltaEn) {
        this.id = Objects.requireNonNull(id);
        this.producto = Objects.requireNonNull(producto);
        this.pedido = Objects.requireNonNull(pedido);
        this.numeroPedido = numeroPedido;
        this.cantidad = cantidad;
        this.creadaEn = Objects.requireNonNull(creadaEn);
        this.estado = Objects.requireNonNull(estado);
        this.resueltaEn = resueltaEn;
    }

    public static Reserva nueva(ProductoId producto, UUID pedido, int numeroPedido, int cantidad, Instant ahora) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Una reserva tiene al menos una unidad");
        }
        return new Reserva(UUID.randomUUID(), producto, pedido, numeroPedido, cantidad, ahora, EstadoDeReserva.PENDIENTE, null);
    }

    public static Reserva reconstruir(UUID id, ProductoId producto, UUID pedido, int numeroPedido, int cantidad,
                                      Instant creadaEn, EstadoDeReserva estado, Instant resueltaEn) {
        return new Reserva(id, producto, pedido, numeroPedido, cantidad, creadaEn, estado, resueltaEn);
    }

    public void cubrir(Instant ahora) {
        exigirPendiente();
        estado = EstadoDeReserva.CUBIERTA;
        resueltaEn = ahora;
    }

    public void cancelar(Instant ahora) {
        exigirPendiente();
        estado = EstadoDeReserva.CANCELADA;
        resueltaEn = ahora;
    }

    public boolean pendiente() {
        return estado == EstadoDeReserva.PENDIENTE;
    }

    private void exigirPendiente() {
        if (!pendiente()) {
            throw new OperacionDeStockInvalidaException("La reserva ya está " + (estado == EstadoDeReserva.CUBIERTA ? "cubierta" : "cancelada"));
        }
    }

    public UUID id() {
        return id;
    }

    public ProductoId producto() {
        return producto;
    }

    public UUID pedido() {
        return pedido;
    }

    public int numeroPedido() {
        return numeroPedido;
    }

    public int cantidad() {
        return cantidad;
    }

    public Instant creadaEn() {
        return creadaEn;
    }

    public EstadoDeReserva estado() {
        return estado;
    }

    public Instant resueltaEn() {
        return resueltaEn;
    }
}
