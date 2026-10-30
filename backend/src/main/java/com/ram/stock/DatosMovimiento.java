package com.ram.stock;

import java.time.Instant;

/**
 * @param cantidad      cuánto cambió (+ entra, − sale)
 * @param saldo         cuántas quedaron después
 * @param numeroPedido  si vino de un pedido; si no, null
 * @param usuario       quién lo hizo (para los de pedidos, quien confirmó o canceló)
 */
public record DatosMovimiento(Instant fecha, TipoDeMovimiento tipo, int cantidad, int saldo, String nota,
                              Integer numeroPedido, String usuario) {
}
