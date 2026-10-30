package com.ram.notificaciones.adaptadores;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ram.correo.* en la configuración.
 *
 * @param remitente        el "De:" de los mails. Con Gmail tiene que ser la misma cuenta que los manda.
 * @param destinoDelTaller a dónde llegan postulaciones, consultas y avisos de pedidos.
 */
@ConfigurationProperties("ram.correo")
public record CorreoProperties(String remitente, String destinoDelTaller) {
}
