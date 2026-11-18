package com.ram.trabajos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Un trabajo realizado, con sus fotos (ids, en orden: la primera es la principal).
 *
 * @param barrioClave el barrio normalizado ("B° Alberdi" y "alberdi" → "alberdi"), para agrupar por zona
 */
public record DatosTrabajo(UUID id, String slug, TipoDeTrabajo tipo, String titulo, String barrio, String barrioClave,
                           boolean aDomicilio, String problema, String diagnostico, String solucion, LocalDate fecha,
                           Integer numeroDeOrden, boolean consentimiento, boolean publicado, List<UUID> fotos) {
}
