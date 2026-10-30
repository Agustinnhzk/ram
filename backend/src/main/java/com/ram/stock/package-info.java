/**
 * Módulo de stock: cuántas unidades hay de cada producto, cada movimiento y las reservas (ADR-014).
 *
 * <p>Un producto puede no tener control de stock (se vende como siempre). Cuando se le carga una cantidad, la tienda
 * muestra si hay, si quedan pocas o si no hay (y se puede reservar). El stock se descuenta cuando el taller confirma
 * el pago de un pedido; lo que falta queda como reserva, que el taller cubre a mano cuando carga mercadería.
 *
 * <p>API: {@link com.ram.stock.Stock} y sus datos. Lo usa pedidos (descontar y devolver), nunca al revés.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Stock")
package com.ram.stock;
