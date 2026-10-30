package com.ram.stock;

/**
 * El stock como lo ve la tienda.
 *
 * @param quedan solo con {@link EstadoDeStock#ULTIMAS_UNIDADES}; en el resto, null (no se publica cuánto hay)
 */
public record DisponibilidadPublica(String slug, EstadoDeStock estado, Integer quedan) {

    public static final int POCAS_UNIDADES = 3;

    public static DisponibilidadPublica de(String slug, int cantidad) {
        if (cantidad <= 0) {
            return new DisponibilidadPublica(slug, EstadoDeStock.SIN_STOCK, null);
        }
        if (cantidad <= POCAS_UNIDADES) {
            return new DisponibilidadPublica(slug, EstadoDeStock.ULTIMAS_UNIDADES, cantidad);
        }
        return new DisponibilidadPublica(slug, EstadoDeStock.DISPONIBLE, null);
    }
}
