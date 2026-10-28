package com.ram.clientes.dominio;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Nombre y apellido del cliente. Así decidimos reconocer a un cliente que vuelve,
 * por eso además del nombre tal como se escribió, sabe dar su forma "para buscar".
 */
public record NombreCompleto(String valor) {

    private static final int LARGO_MAXIMO = 150;

    public NombreCompleto {
        Objects.requireNonNull(valor, "El nombre es obligatorio");
        valor = valor.trim().replaceAll("\\s+", " ");
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (valor.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("El nombre no puede superar " + LARGO_MAXIMO + " caracteres");
        }
        if (!valor.matches(".*\\p{L}.*")) {
            throw new IllegalArgumentException("El nombre tiene que tener al menos una letra");
        }
    }

    /** "Agustín  Pérez" → "agustin perez". Es lo que se guarda en la columna de búsqueda. */
    public String paraBuscar() {
        return normalizar(valor);
    }

    /**
     * Misma transformación para lo que el usuario escribe en el buscador,
     * así "AGUSTIN perez" y "Agustín Pérez" se comparan igual.
     *
     * <p>Quita tildes (á → a, ñ → n), pasa a minúsculas y deja solo letras, números y un espacio
     * entre palabras. Como no quedan símbolos, tampoco quedan comodines de SQL como % o _.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
