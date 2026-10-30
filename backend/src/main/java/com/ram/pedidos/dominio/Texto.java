package com.ram.pedidos.dominio;

/** Validaciones de texto. Los mensajes se le muestran a la persona. */
public final class Texto {

    private Texto() {
    }

    public static String obligatorio(String valor, String campo, int largoMaximo) {
        String limpio = opcional(valor, campo, largoMaximo);
        if (limpio == null) {
            throw new IllegalArgumentException("Falta " + campo);
        }
        return limpio;
    }

    public static String opcional(String valor, String campo, int largoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > largoMaximo) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + largoMaximo + " caracteres");
        }
        return limpio;
    }

    /** Entre 8 y 15 dígitos; se aceptan espacios, guiones, paréntesis y +. Se guarda como lo escribió. */
    public static String telefono(String valor) {
        String limpio = obligatorio(valor, "el teléfono", 30);
        long digitos = limpio.chars().filter(Character::isDigit).count();
        if (!limpio.matches("[0-9 +()\\-]+") || digitos < 8 || digitos > 15) {
            throw new IllegalArgumentException("El teléfono no es válido");
        }
        return limpio;
    }
}
