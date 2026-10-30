package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;

/** Puerto de salida: las sesiones abiertas de un usuario (en todos sus dispositivos). */
public interface SesionesDeUsuario {

    /** Cierra todas. Si alguien entró con la contraseña vieja, queda afuera. */
    void cerrarTodas(UsuarioId usuario);
}
