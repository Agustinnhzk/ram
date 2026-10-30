package com.ram.usuarios.aplicacion;

/** El link no existe, venció o ya se usó. Para la persona es lo mismo: que pida otro. */
public class EnlaceDeRecuperacionInvalidoException extends RuntimeException {

    public EnlaceDeRecuperacionInvalidoException() {
        super("El link para cambiar la contraseña no es válido o ya venció");
    }
}
