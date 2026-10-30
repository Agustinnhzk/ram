package com.ram.pedidos.dominio;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Una línea del pedido, con el precio del momento de la compra. No cambia nunca: si mañana el producto
 * aumenta, este pedido sigue valiendo lo que valía.
 *
 * @param productos qué productos lleva UNA unidad de la línea (ADR-014): un producto suelto, los componentes de una PC,
 *                  o ninguno (el servicio de armado). Es lo que se descuenta del stock al confirmar el pago.
 * @param sinStock  si al hacer el pedido faltaba stock de alguno: queda como reserva
 */
public record Linea(String descripcion, List<String> detalle, String referencia, int cantidad, long precioUnitario,
                    List<UUID> productos, boolean sinStock) {

    public static final int CANTIDAD_MAXIMA = 20;

    public Linea {
        Objects.requireNonNull(descripcion);
        Objects.requireNonNull(referencia);
        detalle = detalle == null ? List.of() : List.copyOf(detalle);
        productos = productos == null ? List.of() : List.copyOf(productos);
        if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad tiene que ser entre 1 y " + CANTIDAD_MAXIMA);
        }
        if (precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio tiene que ser mayor a cero");
        }
    }

    /** Sin productos asociados (pedidos anteriores al ADR-014, y tests). */
    public Linea(String descripcion, List<String> detalle, String referencia, int cantidad, long precioUnitario) {
        this(descripcion, detalle, referencia, cantidad, precioUnitario, List.of(), false);
    }

    /** No desborda: 20 unidades × el precio máximo del catálogo (ADR-008) entra de sobra en un long. */
    public long subtotal() {
        return Math.multiplyExact(precioUnitario, (long) cantidad);
    }

    public Linea conSinStock(boolean falta) {
        return new Linea(descripcion, detalle, referencia, cantidad, precioUnitario, productos, falta);
    }
}
