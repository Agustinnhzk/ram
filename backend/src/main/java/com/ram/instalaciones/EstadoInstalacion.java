package com.ram.instalaciones;

/** Se puede pasar de cualquiera a cualquiera: es un registro, no un circuito con reglas. */
public enum EstadoInstalacion {
    /** Hay que ir a ver el lugar (la visita se cobra según la distancia, a consultar). */
    VISITA,
    PRESUPUESTO,
    INSTALACION,
    /** Ya está instalada: sale del listado del día. */
    TERMINADA
}
