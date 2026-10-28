package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;

import java.util.UUID;

/** Lo que ve el frontend de la sesión: sin el id del taller ni nada interno. */
record SesionResponse(UUID id, String nombre, TipoDeCuenta tipo, Rol rol) {

    static SesionResponse de(UsuarioAutenticado u) {
        return new SesionResponse(u.id(), u.nombre(), u.tipo(), u.rol());
    }
}
