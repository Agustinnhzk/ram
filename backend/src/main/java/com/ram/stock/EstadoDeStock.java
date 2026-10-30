package com.ram.stock;

/** Lo que la tienda muestra de un producto con control de stock. */
public enum EstadoDeStock {
    DISPONIBLE,
    /** Quedan pocas (hasta {@link DisponibilidadPublica#POCAS_UNIDADES}): la tienda muestra "Últimas N". */
    ULTIMAS_UNIDADES,
    /** No hay: se puede comprar igual, y queda reservado hasta que llegue. */
    SIN_STOCK
}
