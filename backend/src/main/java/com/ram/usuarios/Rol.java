package com.ram.usuarios;

/**
 * Roles del personal del taller. RAM usa solo DUENO; los otros dos existen
 * para los talleres del SaaS que tengan empleados.
 */
public enum Rol {
    /** Todo: precios, stock, configuración, alta de empleados. */
    DUENO,
    /** Diagnósticos y reparaciones. Puede ver el password de los equipos. */
    TECNICO,
    /** Ingresos, salidas, clientes y cobros. No ve passwords de equipos. */
    MOSTRADOR
}
