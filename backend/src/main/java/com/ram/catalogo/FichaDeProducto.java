package com.ram.catalogo;

import java.util.List;

/**
 * Lo que se carga en el formulario de un producto (todo menos el precio, la oferta y las fotos, que tienen
 * su propio camino). La valida el dominio; los mensajes se le pueden mostrar a la persona.
 *
 * @param categoria  slug de la categoría
 * @param tecnicos   null si no es un componente de Armá tu PC
 */
public record FichaDeProducto(
        String nombre,
        String categoria,
        Condicion condicion,
        String resumen,
        String descripcion,
        List<String> caracteristicas,
        Integer garantiaMeses,
        boolean publicado,
        DatosTecnicos tecnicos) {
}
