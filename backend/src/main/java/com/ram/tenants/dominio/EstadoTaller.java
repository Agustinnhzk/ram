package com.ram.tenants.dominio;

public enum EstadoTaller {
    /** Opera normalmente: su tienda y su sistema responden. */
    ACTIVO,
    /** Dado de baja temporalmente (por ejemplo, falta de pago del SaaS): no responde. */
    SUSPENDIDO
}
