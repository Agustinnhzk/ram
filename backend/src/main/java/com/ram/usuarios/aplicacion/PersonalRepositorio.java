package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.Personal;

import java.util.Optional;

/** Puerto de salida. Todo es del taller actual (ADR-005). */
public interface PersonalRepositorio {

    void guardar(Personal personal);

    Optional<Personal> buscarPorId(UsuarioId id);

    Optional<Personal> buscarPorCorreo(CorreoDeAcceso correo);

    boolean existeCorreo(CorreoDeAcceso correo);

    boolean hayAlguno();
}
