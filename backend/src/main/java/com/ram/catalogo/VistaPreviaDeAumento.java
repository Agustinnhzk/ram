package com.ram.catalogo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que pasaría si se aplica un aumento, SIN aplicarlo. Los productos sin precio no se tocan.
 *
 * @param categoria null = todos los productos
 */
public record VistaPreviaDeAumento(BigDecimal porcentaje, String categoria, List<PrecioPropuesto> precios) {
}
