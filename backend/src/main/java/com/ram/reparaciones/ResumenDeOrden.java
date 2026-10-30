package com.ram.reparaciones;

import java.time.Instant;
import java.time.LocalDate;

/** Una fila del listado de reparaciones. */
public record ResumenDeOrden(
        OrdenId id,
        int numero,
        String cliente,
        String telefono,
        String equipo,
        EstadoReparacion estado,
        Instant ingresadoEn,
        LocalDate garantiaHasta,
        boolean enGarantia) {
}
