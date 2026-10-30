/**
 * Módulo de pedidos: la compra en la tienda con pago por transferencia (ADR-011).
 *
 * <p>El cliente arma el carrito, confirma el pedido (el servidor recalcula los precios con el catálogo),
 * transfiere al alias del taller y sube el comprobante. El taller lo revisa en el sistema y lo marca
 * pagado, lo rechaza o lo entrega.
 *
 * <p>Usa la API pública de catálogo (precios), usuarios (quién compra), archivos (el comprobante)
 * y notificaciones (avisos por mail, con eventos).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Pedidos")
package com.ram.pedidos;
