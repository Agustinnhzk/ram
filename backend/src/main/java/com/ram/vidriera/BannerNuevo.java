package com.ram.vidriera;

import java.time.LocalDate;

/**
 * Lo que se carga en el formulario de un banner.
 *
 * @param textoAlternativo lo que dice el flyer, en palabras: lo leen los lectores de pantalla y Google
 * @param enlace           a dónde lleva al tocarlo: una ruta de la tienda ("/productos?categoria=camaras")
 */
public record BannerNuevo(String textoAlternativo, String enlace, boolean activo, LocalDate desde, LocalDate hasta) {
}
