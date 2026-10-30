package com.ram.reparaciones.dominio;

import com.ram.usuarios.UsuarioId;

import java.util.Objects;

/** Quién hizo algo. Se guarda el nombre de ese momento, igual que en el historial de precios. */
public record Autor(UsuarioId id, String nombre) {

    public Autor {
        Objects.requireNonNull(id);
        Objects.requireNonNull(nombre);
    }
}
