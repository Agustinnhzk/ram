package com.ram.usuarios.aplicacion;

/**
 * Correo o contraseña incorrectos. Siempre el mismo error, a propósito:
 * si dijéramos "ese correo no existe", cualquiera podría averiguar quién tiene cuenta.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
