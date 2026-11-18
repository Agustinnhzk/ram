/**
 * Módulo de trabajos realizados (ADR-018): casos reales que se publican en la tienda para mostrar qué hacemos
 * y en qué barrios ("Notebook que no encendía — Abasto").
 *
 * <p>Se cargan a mano desde el sistema, con fotos. Se publican solo con el permiso del cliente, y nunca con
 * calle ni altura: solo el barrio o la localidad. Como todo dato por taller, el filtro lo aplica la persistencia (ADR-005).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Trabajos realizados")
package com.ram.trabajos;
