package com.ram.stock.dominio;

/** Las notas de los movimientos: opcionales (salvo el motivo de un ajuste), una línea, hasta 300 caracteres. */
public final class Nota {

    public static final int LARGO_MAXIMO = 300;

    private Nota() {
    }

    public static String opcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.strip().replaceAll("\\s+", " ");
        if (limpio.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("La nota no puede pasar de " + LARGO_MAXIMO + " caracteres");
        }
        return limpio;
    }

    public static String obligatoria(String texto, String que) {
        String limpio = opcional(texto);
        if (limpio == null) {
            throw new IllegalArgumentException("Escribí " + que);
        }
        return limpio;
    }
}
