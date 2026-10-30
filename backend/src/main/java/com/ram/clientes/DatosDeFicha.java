package com.ram.clientes;

/** Lo que se edita de una ficha desde el sistema. @param correo y notas pueden venir vacíos */
public record DatosDeFicha(String nombreCompleto, String telefono, String correo, String notas) {
}
