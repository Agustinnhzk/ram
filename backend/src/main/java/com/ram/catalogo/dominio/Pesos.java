package com.ram.catalogo.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Un precio en pesos argentinos, sin centavos.
 *
 * <p>Es un "value object": se valida al crearse, así no puede existir un precio negativo o absurdo
 * en ningún lado del sistema. Y nunca usa double: 0.1 + 0.2 da 0.30000000000000004 en double,
 * y con plata eso no puede pasar. Los cálculos van con BigDecimal y el resultado es un número entero.
 */
public record Pesos(long valor) {

    /** Un billón de pesos: más que eso es un error de tipeo (un cero de más), no un precio. */
    static final long MAXIMO = 1_000_000_000_000L;

    public Pesos {
        if (valor < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (valor > MAXIMO) {
            throw new IllegalArgumentException("El precio es demasiado alto: revisá que no sobre un cero");
        }
    }

    /**
     * El precio con el porcentaje aplicado, redondeado al peso más cercano (las mitades, para arriba).
     * $ 41.237 + 8% = $ 44.535,96 → $ 44.536.
     */
    public Pesos aplicar(Porcentaje porcentaje) {
        BigDecimal resultado = BigDecimal.valueOf(valor)
                .multiply(porcentaje.comoFactor())
                .setScale(0, RoundingMode.HALF_UP);
        return new Pesos(resultado.longValueExact());
    }
}
