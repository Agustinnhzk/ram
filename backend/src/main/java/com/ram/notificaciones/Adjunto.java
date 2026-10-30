package com.ram.notificaciones;

import java.util.Objects;

/** Un archivo adjunto a un mail. */
public record Adjunto(String nombre, String tipoDeContenido, byte[] contenido) {

    public Adjunto {
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(tipoDeContenido);
        Objects.requireNonNull(contenido);
    }

    /** Sin los bytes: si un log imprime el adjunto, que no vuelque el archivo entero. */
    @Override
    public String toString() {
        return "Adjunto[" + nombre + ", " + tipoDeContenido + ", " + contenido.length + " bytes]";
    }
}
