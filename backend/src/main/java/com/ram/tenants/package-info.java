/**
 * Módulo de talleres (tenants).
 *
 * <p>Spring Modulith toma este paquete como un módulo. Solo los tipos que viven
 * directamente acá ({@link com.ram.tenants.TallerId} y {@link com.ram.tenants.TallerActual})
 * son la API pública: los subpaquetes (dominio, aplicacion, adaptadores) son internos
 * y ningún otro módulo puede usarlos. Si alguno lo intenta, el test de módulos falla.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Talleres")
package com.ram.tenants;
