package com.ram.reparaciones;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Lo que ve el CLIENTE de su reparación. A propósito no tiene password, observaciones,
 * notas internas ni nombres del personal (ADR-009).
 */
public record Seguimiento(
        int numero,
        String equipo,
        EstadoReparacion estado,
        Long presupuestoMonto,
        String presupuestoDescripcion,
        Instant ingresadoEn,
        LocalDate garantiaHasta,
        boolean enGarantia,
        List<Paso> pasos) {

    /** Un paso visible para el cliente: el estado al que pasó (o null si fue un aviso) y el mensaje. */
    public record Paso(EstadoReparacion estado, String nota, Instant cuando) {
    }
}
