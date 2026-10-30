package com.ram.reparaciones.dominio;

/** Validaciones de texto que se repiten en el módulo. */
final class Texto {

    private Texto() {
    }

    /** Obligatorio: sin espacios de más, no vacío y con un largo máximo. */
    static String obligatorio(String valor, String campo, int largoMaximo) {
        String limpio = opcional(valor, campo, largoMaximo);
        if (limpio == null) {
            throw new IllegalArgumentException("Falta " + campo);
        }
        return limpio;
    }

    /** Opcional: vacío o solo espacios → null. */
    static String opcional(String valor, String campo, int largoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > largoMaximo) {
            throw new IllegalArgumentException(primeraMayuscula(campo) + " no puede superar " + largoMaximo + " caracteres");
        }
        return limpio;
    }

    private static String primeraMayuscula(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
