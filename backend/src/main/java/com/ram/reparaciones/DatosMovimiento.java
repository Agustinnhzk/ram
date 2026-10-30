package com.ram.reparaciones;

import java.time.Instant;

/**
 * Una línea de la bitácora.
 *
 * @param estado      el estado al que pasó, o null si fue solo una nota
 * @param paraCliente true si aparece en el seguimiento del cliente
 */
public record DatosMovimiento(EstadoReparacion estado, String nota, boolean paraCliente, String usuario, Instant cuando) {
}
