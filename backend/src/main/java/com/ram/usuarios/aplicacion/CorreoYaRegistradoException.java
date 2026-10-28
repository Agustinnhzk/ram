package com.ram.usuarios.aplicacion;

public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException() {
        super("Ya existe una cuenta con ese correo");
    }
}
