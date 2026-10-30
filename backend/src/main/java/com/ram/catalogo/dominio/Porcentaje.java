package com.ram.catalogo.dominio;

import java.math.BigDecimal;

/**
 * Un porcentaje de aumento (positivo) o de rebaja (negativo): 8 = +8%, -10 = -10%.
 *
 * <p>Los límites protegen de errores de tipeo que afectarían a todo el catálogo de una:
 * escribir 800 en vez de 8, o -100 (todo gratis).
 */
public record Porcentaje(BigDecimal valor) {

    static final BigDecimal MINIMO = new BigDecimal("-50");
    static final BigDecimal MAXIMO = new BigDecimal("100");
    private static final BigDecimal CIEN = new BigDecimal("100");

    public Porcentaje {
        if (valor == null) {
            throw new IllegalArgumentException("Falta el porcentaje");
        }
        if (valor.signum() == 0) {
            throw new IllegalArgumentException("El porcentaje no puede ser 0");
        }
        if (valor.compareTo(MINIMO) < 0 || valor.compareTo(MAXIMO) > 0) {
            throw new IllegalArgumentException("El porcentaje tiene que estar entre -50% y +100%");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El porcentaje admite hasta 2 decimales");
        }
    }

    /** 8 → 1.08; -10 → 0.90. Es lo que se multiplica por el precio. */
    BigDecimal comoFactor() {
        return BigDecimal.ONE.add(valor.divide(CIEN));
    }
}
