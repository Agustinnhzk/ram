/**
 * Módulo de notificaciones: mandar mails. Lo usan otros módulos (contactos, pedidos) a través de
 * {@link com.ram.notificaciones.Correos}, sin saber cómo se mandan (SMTP, un servicio externo, o el log en desarrollo).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Notificaciones")
package com.ram.notificaciones;
