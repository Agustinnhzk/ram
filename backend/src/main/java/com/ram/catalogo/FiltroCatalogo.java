package com.ram.catalogo;

/**
 * Los filtros de la tienda: /productos?categoria=camaras&condicion=nuevo&q=dvr. Todos opcionales (null).
 *
 * @param texto lo que se escribió en el buscador; se busca sin importar tildes ni mayúsculas
 */
public record FiltroCatalogo(String categoria, Condicion condicion, String texto) {

    public static FiltroCatalogo todo() {
        return new FiltroCatalogo(null, null, null);
    }
}
