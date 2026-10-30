package com.ram.pedidos;

import com.ram.catalogo.TipoComponente;

import java.util.Map;

/**
 * Lo que el cliente pide, tal como viene del carrito: QUÉ quiere, nunca cuánto cuesta.
 * El precio lo pone el servidor con el catálogo (el carrito vive en el navegador y se puede editar).
 */
public sealed interface ItemPedido {

    int cantidad();

    /** Un producto de la tienda, por su slug. */
    record Producto(String slug, int cantidad) implements ItemPedido {
    }

    /**
     * Una PC de Armá tu PC: el slug de cada componente.
     *
     * @param sinPlacaDeVideo usa los gráficos del procesador (solo si los tiene)
     * @param conArmado       si quiere que el taller la arme y la pruebe (se cobra aparte), o solo los componentes
     */
    record PcArmada(Map<TipoComponente, String> piezas, boolean sinPlacaDeVideo, boolean conArmado, int cantidad)
            implements ItemPedido {

        public PcArmada {
            piezas = piezas == null ? Map.of() : Map.copyOf(piezas);
        }
    }
}
