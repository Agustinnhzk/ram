package com.ram.trabajos.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * El barrio o la localidad de un trabajo, escrito libre en el sistema.
 *
 * <p>Se guarda de dos formas:
 * <ul>
 *   <li><b>texto:</b> como se muestra. Se le saca el "Barrio"/"B°" del principio (la página ya dice "barrio")
 *       y, si se escribió todo en minúscula, se ponen las mayúsculas: "b° alberdi" → "Alberdi".</li>
 *   <li><b>clave:</b> para agrupar. "Alberdi", "B° Alberdi" y "alberdi" son la misma zona: "alberdi".</li>
 * </ul>
 */
public record Barrio(String texto, String clave) {

    static final int LARGO_MAXIMO = 60;

    /** "Barrio ", "B° " (o "B°Alberdi" pegado), "Bº ", "B. ", "Bo ", "B " al principio. */
    private static final Pattern PREFIJO = Pattern.compile("^(?:barrio(?:\\s+|$)|b\\s*[°º.]\\s*|bo\\.?\\s+|b\\s+)",
            Pattern.CASE_INSENSITIVE);
    /** Una calle con altura ("Mendoza 1234") o un número largo: no se publica la dirección del cliente. */
    private static final Pattern PARECE_DIRECCION = Pattern.compile(".*\\d{3,}.*");

    public Barrio {
        Objects.requireNonNull(texto);
        Objects.requireNonNull(clave);
    }

    public static Barrio de(String escrito) {
        String limpio = escrito == null ? "" : escrito.strip().replaceAll("\\s+", " ");
        limpio = PREFIJO.matcher(limpio).replaceFirst("").strip();
        if (limpio.length() < 2 || limpio.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("Escribí el barrio o la localidad (por ejemplo Alberdi o Funes), hasta "
                    + LARGO_MAXIMO + " caracteres");
        }
        if (PARECE_DIRECCION.matcher(limpio).matches()) {
            throw new IllegalArgumentException("Poné solo el barrio o la localidad, sin calle ni altura: es la casa del cliente");
        }
        String texto = limpio.equals(limpio.toLowerCase(Locale.ROOT)) ? conMayusculas(limpio) : limpio;
        return new Barrio(texto, Slug.de(texto));
    }

    /** "villa gobernador galvez" → "Villa Gobernador Galvez". Las palabras cortas ("de", "del") quedan en minúscula. */
    private static String conMayusculas(String texto) {
        StringBuilder resultado = new StringBuilder();
        String[] palabras = texto.split(" ");
        for (int i = 0; i < palabras.length; i++) {
            String p = palabras[i];
            boolean corta = i > 0 && (p.equals("de") || p.equals("del") || p.equals("la") || p.equals("las")
                    || p.equals("los") || p.equals("y"));
            if (i > 0) {
                resultado.append(' ');
            }
            resultado.append(corta || p.isEmpty() ? p : Character.toUpperCase(p.charAt(0)) + p.substring(1));
        }
        return resultado.toString();
    }
}
