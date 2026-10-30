package com.ram.catalogo.dominio;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Para que el buscador de la tienda encuentre "Cámaras" escribiendo "camara":
 * sin tildes, en minúsculas, y solo letras y números separados por un espacio.
 */
public final class TextoDeBusqueda {

    private TextoDeBusqueda() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** "Kit DVR" → ["kit", "dvr"]. Vacío si no hay nada para buscar. */
    public static List<String> palabras(String texto) {
        String normalizado = normalizar(texto);
        return normalizado.isEmpty() ? List.of() : Arrays.asList(normalizado.split(" "));
    }
}
