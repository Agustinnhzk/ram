package com.ram.instalaciones.dominio;

/** Textos de los formularios: sin espacios de más, con un largo máximo. */
public final class Textos {

    private Textos() {
    }

    public static String opcional(String texto, String que, int maximo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.strip();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El texto de " + que + " no puede pasar de " + maximo + " caracteres");
        }
        return limpio;
    }

    public static String obligatorio(String texto, String que, int maximo) {
        String limpio = opcional(texto, que, maximo);
        if (limpio == null) {
            throw new IllegalArgumentException("Falta " + que);
        }
        return limpio;
    }
}
