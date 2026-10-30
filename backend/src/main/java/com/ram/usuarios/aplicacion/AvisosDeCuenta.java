package com.ram.usuarios.aplicacion;

/** Puerto de salida: avisarle algo al dueño de una cuenta (hoy, por mail). */
public interface AvisosDeCuenta {

    /** Manda el link para elegir una contraseña nueva. No espera a que salga el mail. */
    void enviarEnlaceDeRecuperacion(String correo, String nombre, String enlace);
}
