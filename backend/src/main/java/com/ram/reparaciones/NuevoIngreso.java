package com.ram.reparaciones;

import com.ram.clientes.NuevoCliente;

import java.util.UUID;

/**
 * Lo que se carga en el formulario de ingreso. El cliente va de una de dos formas:
 * {@code clienteId} si ya existe (un cliente que vuelve), o {@code clienteNuevo} si es la primera vez.
 *
 * @param numero      el número de orden que escribe el taller (900, 901...)
 * @param password    del equipo, si el cliente lo dejó (puede ser null)
 * @param problema    lo que dice el cliente que le pasa
 * @param observaciones internas del taller (estado físico, rayones...): nunca salen al cliente
 */
public record NuevoIngreso(
        Integer numero,
        UUID clienteId,
        NuevoCliente clienteNuevo,
        String equipo,
        boolean conCargador,
        String procesador,
        String password,
        String problema,
        String observaciones) {

    /** El password nunca aparece en logs ni en mensajes de error (ADR-009). */
    @Override
    public String toString() {
        return "NuevoIngreso[numero=" + numero + ", equipo=" + equipo + ", password=" + (password == null ? "no" : "***") + "]";
    }
}
