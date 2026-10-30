package com.ram.pedidos;

import java.util.EnumSet;
import java.util.Set;

/**
 * <pre>
 * ESPERANDO_PAGO ──(sube comprobante)──▶ PAGO_INFORMADO ──(el taller confirma)──▶ PAGADO ──▶ ENTREGADO
 *       ▲                                      │
 *       └────────(el taller lo rechaza)────────┘
 * CANCELADO: desde cualquiera antes de ENTREGADO.
 * </pre>
 * El taller también puede confirmar el pago desde ESPERANDO_PAGO (por ejemplo, pagó en efectivo al retirar).
 */
public enum EstadoPedido {
    ESPERANDO_PAGO,
    PAGO_INFORMADO,
    PAGADO,
    ENTREGADO,
    CANCELADO;

    /** Los que el taller tiene que mirar: falta revisar un pago o entregar. */
    public static final Set<EstadoPedido> ACTIVOS = EnumSet.of(ESPERANDO_PAGO, PAGO_INFORMADO, PAGADO);

    public boolean terminado() {
        return this == ENTREGADO || this == CANCELADO;
    }
}
