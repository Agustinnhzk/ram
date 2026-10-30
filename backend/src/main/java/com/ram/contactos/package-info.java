/**
 * Módulo de contactos: lo que llega desde "Trabajá con nosotros" de la tienda.
 * <ul>
 *   <li><b>Postulaciones</b> de personas que quieren trabajar en el taller, con su CV.</li>
 *   <li><b>Consultas de empresas</b> que quieren tercerizar su soporte IT o hacer una alianza comercial.</li>
 * </ul>
 * No se guardan en la base: se mandan por mail al taller (módulo notificaciones). Ver ADR-010.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Contactos")
package com.ram.contactos;
