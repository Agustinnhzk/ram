package com.ram.pedidos;

import java.util.List;

/**
 * Lo que manda el checkout. Lo valida el servicio.
 *
 * @param direccion obligatoria si la entrega es ENVIO
 */
public record NuevoPedido(List<ItemPedido> items, FormaDeEntrega entrega, String direccion, String telefono, String nota) {

    public NuevoPedido {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
