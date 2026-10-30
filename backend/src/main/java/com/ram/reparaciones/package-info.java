/**
 * Módulo de reparaciones: órdenes de reparación con su recorrido de estados, bitácora,
 * presupuesto, entrega con garantía, y el seguimiento que ve el cliente (ADR-009).
 *
 * <p>Usa la API pública de clientes (para el cliente de cada orden) y de usuarios (quién hace cada cosa).
 * API pública: {@link com.ram.reparaciones.Reparaciones} y los tipos de este paquete.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Reparaciones")
package com.ram.reparaciones;
