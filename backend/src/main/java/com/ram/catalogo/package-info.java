/**
 * Módulo de catálogo: categorías, productos (incluidos los componentes de Armá tu PC) y sus precios.
 *
 * <p>API pública: {@link com.ram.catalogo.Catalogo} y los tipos de este paquete. Otros módulos
 * (por ejemplo, ventas cuando exista) consultan precios por acá; nunca tocan las tablas.
 *
 * <p>Los precios son en pesos, sin centavos. Cada cambio queda en el historial con quién y cuándo.
 * Como todo dato por taller, el filtro lo aplica la persistencia (ADR-005).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Catálogo")
package com.ram.catalogo;
