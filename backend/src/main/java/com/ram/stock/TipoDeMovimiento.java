package com.ram.stock;

/** Por qué cambió el stock. */
public enum TipoDeMovimiento {
    /** Se empezó a controlar el stock, con lo que había. */
    INICIAL,
    /** Llegó mercadería. */
    INGRESO,
    /** Se contó y había otra cantidad. */
    AJUSTE,
    /** Se confirmó el pago de un pedido. */
    VENTA,
    /** Se usaron unidades nuevas para una reserva. */
    RESERVA_CUBIERTA,
    /** Se canceló un pedido pagado y las unidades volvieron. */
    DEVOLUCION
}
