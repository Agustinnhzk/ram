package com.ram.catalogo.dominio;

import java.util.Objects;
import java.util.UUID;

/** Una categoría del catálogo de un taller (cámaras, alarmas...). Cada taller tiene las suyas. */
public record Categoria(UUID id, String slug, String nombre, boolean conInstalacion) {

    public Categoria {
        Objects.requireNonNull(id);
        Objects.requireNonNull(slug);
        Objects.requireNonNull(nombre);
    }
}
