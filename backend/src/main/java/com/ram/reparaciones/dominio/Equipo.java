package com.ram.reparaciones.dominio;

/**
 * El equipo que deja el cliente.
 *
 * @param descripcion "Notebook Lenovo IdeaPad 3", "PC de escritorio"...
 * @param procesador  opcional
 */
public record Equipo(String descripcion, boolean conCargador, String procesador) {

    public Equipo {
        descripcion = Texto.obligatorio(descripcion, "el equipo", 150);
        procesador = Texto.opcional(procesador, "el procesador", 100);
    }
}
