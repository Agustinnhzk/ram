package com.ram.clientes;

/**
 * Datos para dar de alta un cliente, tal como llegan del formulario de ingreso.
 *
 * @param correo puede ser null: un cliente sin mail recibe el comprobante impreso o por WhatsApp
 */
public record NuevoCliente(String nombreCompleto, String telefono, String correo) {
}
