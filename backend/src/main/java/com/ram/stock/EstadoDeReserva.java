package com.ram.stock;

public enum EstadoDeReserva {
    /** Pagada, esperando mercadería. */
    PENDIENTE,
    /** Ya se separaron las unidades. */
    CUBIERTA,
    /** Se canceló el pedido. */
    CANCELADA
}
