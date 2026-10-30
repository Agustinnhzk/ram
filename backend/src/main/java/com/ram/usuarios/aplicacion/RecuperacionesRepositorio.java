package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.Recuperacion;

import java.util.List;
import java.util.Optional;

/** Puerto de salida. Todo es del taller actual: un link de un taller no sirve en otro. */
public interface RecuperacionesRepositorio {

    void guardar(Recuperacion recuperacion);

    Optional<Recuperacion> porHash(String tokenHash);

    /** Las que todavía no se usaron (para anularlas al pedir un link nuevo). */
    List<Recuperacion> sinUsarDe(UsuarioId cuenta);
}
