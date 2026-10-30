package com.ram.notificaciones;

/** El servidor de correo no aceptó el mail (caído, credenciales vencidas...). */
public class CorreoNoEnviadoException extends RuntimeException {

    public CorreoNoEnviadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
