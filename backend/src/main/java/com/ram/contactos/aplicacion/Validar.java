package com.ram.contactos.aplicacion;

import java.util.regex.Pattern;

/** Validaciones de los formularios públicos. Los mensajes se le muestran a la persona. */
final class Validar {

    /** Algo@algo.algo, sin espacios. No intenta ser perfecto: el que valida de verdad es responder el mail. */
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validar() {
    }

    static String obligatorio(String valor, String campo, int largoMaximo) {
        String limpio = opcional(valor, campo, largoMaximo);
        if (limpio == null) {
            throw new IllegalArgumentException("Falta " + campo);
        }
        return limpio;
    }

    static String opcional(String valor, String campo, int largoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > largoMaximo) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + largoMaximo + " caracteres");
        }
        return limpio;
    }

    static String correo(String valor) {
        String limpio = obligatorio(valor, "el correo", 254);
        if (!CORREO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        return limpio;
    }

    /** Se aceptan espacios, guiones, paréntesis y +; tiene que tener entre 8 y 15 dígitos. Se guarda como lo escribió. */
    static String telefono(String valor) {
        String limpio = obligatorio(valor, "el teléfono", 30);
        long digitos = limpio.chars().filter(Character::isDigit).count();
        if (!limpio.matches("[0-9 +()\\-]+") || digitos < 8 || digitos > 15) {
            throw new IllegalArgumentException("El teléfono no es válido");
        }
        return limpio;
    }

    /**
     * Para el asunto del mail: una sola línea. Un salto de línea en el asunto es un intento clásico de
     * meter encabezados falsos en el mail (la librería ya lo frena, pero mejor no depender de eso).
     */
    static String unaLinea(String texto) {
        return texto.replaceAll("[\\r\\n\\t]+", " ");
    }
}
