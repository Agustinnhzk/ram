/**
 * Módulo de archivos subidos (CVs, comprobantes de pago). Lo usan contactos y pedidos.
 *
 * <p>Regla central: el tipo de un archivo se decide por sus <b>primeros bytes</b> ("firma"), no por el nombre
 * ni por lo que dice el navegador. Cualquiera puede renombrar {@code virus.exe} a {@code comprobante.pdf}
 * o mandar el encabezado Content-Type que quiera; la firma no se puede disimular sin romper el archivo.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Archivos")
package com.ram.archivos;
