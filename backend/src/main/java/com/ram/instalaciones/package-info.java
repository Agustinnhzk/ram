/**
 * Módulo de instalaciones y monitoreo (ADR-015). Es un registro simple:
 * <ul>
 *   <li><b>Instalaciones</b> de cámaras y alarmas: visita → presupuesto → instalación → terminada.</li>
 *   <li><b>Monitoreo</b>: los abonados y una planilla de enero a diciembre con quién pagó cada mes.</li>
 * </ul>
 * Los clientes son las mismas fichas del módulo clientes.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Instalaciones y monitoreo")
package com.ram.instalaciones;
