package com.ram.catalogo.dominio;

/** Validaciones de texto del catálogo. Los mensajes se le muestran a la persona. */
final class Texto {

    private Texto() {
    }

    static String obligatorio(String valor, String campo, int largoMaximo) {
        String limpio = opcional(valor, campo, largoMaximo);
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Falta " + campo);
        }
        return limpio;
    }

    /** Opcional: vacío → "" (las columnas de texto del catálogo no admiten NULL). */
    static String opcional(String valor, String campo, int largoMaximo) {
        String limpio = valor == null ? "" : valor.strip();
        if (limpio.length() > largoMaximo) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + largoMaximo + " caracteres");
        }
        return limpio;
    }
}
