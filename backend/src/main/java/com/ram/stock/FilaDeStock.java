package com.ram.stock;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.TipoComponente;

/**
 * Una fila de la pantalla de stock.
 *
 * @param tipoComponente null si no es un componente de Armá tu PC
 * @param cantidad       null si el producto no tiene control de stock
 * @param reservado      unidades reservadas que esperan mercadería
 */
public record FilaDeStock(ProductoId id, String slug, String nombre, String categoria, TipoComponente tipoComponente,
                          boolean publicado, Integer cantidad, int reservado) {

    public boolean controlado() {
        return cantidad != null;
    }
}
