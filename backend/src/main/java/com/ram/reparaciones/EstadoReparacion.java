package com.ram.reparaciones;

import java.util.Set;

/**
 * El recorrido de una reparación (ADR-009). Cada estado sabe a cuáles puede pasar:
 * así es imposible, por ejemplo, entregar un equipo que nunca se diagnosticó.
 */
public enum EstadoReparacion {
    INGRESADO,
    EN_DIAGNOSTICO,
    PRESUPUESTADO,
    APROBADO,
    RECHAZADO,
    EN_REPARACION,
    ESPERANDO_REPUESTO,
    EN_PROVEEDOR,
    SIN_REPARACION,
    LISTO_PARA_RETIRAR,
    ENTREGADO;

    /** A qué estados se puede pasar desde este. ENTREGADO es el final: no pasa a ninguno. */
    public Set<EstadoReparacion> siguientes() {
        return switch (this) {
            case INGRESADO -> Set.of(EN_DIAGNOSTICO);
            case EN_DIAGNOSTICO -> Set.of(PRESUPUESTADO, SIN_REPARACION);
            case PRESUPUESTADO -> Set.of(APROBADO, RECHAZADO);
            case APROBADO -> Set.of(EN_REPARACION);
            case EN_REPARACION -> Set.of(ESPERANDO_REPUESTO, EN_PROVEEDOR, LISTO_PARA_RETIRAR, SIN_REPARACION);
            case ESPERANDO_REPUESTO -> Set.of(EN_REPARACION);
            case EN_PROVEEDOR -> Set.of(EN_REPARACION, LISTO_PARA_RETIRAR);
            case RECHAZADO, SIN_REPARACION -> Set.of(LISTO_PARA_RETIRAR);
            case LISTO_PARA_RETIRAR -> Set.of(ENTREGADO);
            case ENTREGADO -> Set.of();
        };
    }

    public boolean puedePasarA(EstadoReparacion destino) {
        return siguientes().contains(destino);
    }

    /** El equipo todavía está en el taller (no se entregó). */
    public boolean enElTaller() {
        return this != ENTREGADO;
    }
}
