package com.ram.stock.dominio;

import com.ram.catalogo.ProductoId;
import com.ram.stock.TipoDeMovimiento;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Una línea del historial de stock. Nunca se modifica ni se borra: el stock de hoy se puede explicar
 * leyendo los movimientos en orden.
 *
 * @param cantidad cuánto cambió (+ entra, − sale; 0 solo en un INICIAL vacío)
 * @param saldo    cuántas quedaron
 * @param pedido   el pedido que lo causó, o null
 */
public record Movimiento(UUID id, ProductoId producto, TipoDeMovimiento tipo, int cantidad, int saldo, String nota,
                         UUID pedido, Integer numeroPedido, String usuario, Instant fecha) {

    public Movimiento {
        Objects.requireNonNull(id);
        Objects.requireNonNull(producto);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(fecha);
        if (saldo < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
    }

    public static Movimiento de(Existencia e, TipoDeMovimiento tipo, int cantidad, String nota, UUID pedido,
                                Integer numeroPedido, String usuario, Instant fecha) {
        return new Movimiento(UUID.randomUUID(), e.producto(), tipo, cantidad, e.cantidad(), nota, pedido, numeroPedido,
                usuario, fecha);
    }
}
