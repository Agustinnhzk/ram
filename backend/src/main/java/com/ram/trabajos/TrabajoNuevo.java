package com.ram.trabajos;

import java.time.LocalDate;

/**
 * Lo que se carga en el formulario de un trabajo.
 *
 * @param barrio         el barrio o la localidad, escrito libre ("Alberdi", "Funes"). Nunca calle ni altura.
 * @param diagnostico    opcional
 * @param video          opcional: el link de un reel de Instagram o un video de YouTube
 * @param numeroDeOrden  opcional y solo para el taller: no se publica
 * @param consentimiento el cliente aceptó que se publique (sin esto no se puede publicar)
 */
public record TrabajoNuevo(TipoDeTrabajo tipo, String titulo, String barrio, boolean aDomicilio, String problema,
                           String diagnostico, String solucion, String video, LocalDate fecha, Integer numeroDeOrden,
                           boolean consentimiento, boolean publicado) {
}
