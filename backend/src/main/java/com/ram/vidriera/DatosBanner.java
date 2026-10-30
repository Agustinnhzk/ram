package com.ram.vidriera;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Un banner (flyer) de la portada.
 *
 * @param imagenHeredada ruta de una imagen de la web (datos de ejemplo), o null si la imagen se subió desde el sistema
 * @param version        cambia cada vez que se sube otra imagen: va en la URL, así el navegador no muestra la vieja
 * @param desde          primer día que se muestra (null = desde ya)
 * @param hasta          último día que se muestra (null = sin fin)
 * @param visibleHoy     activo y dentro de las fechas
 */
public record DatosBanner(UUID id, String textoAlternativo, String enlace, String imagenHeredada, long version,
                          boolean activo, LocalDate desde, LocalDate hasta, int orden, boolean visibleHoy) {
}
