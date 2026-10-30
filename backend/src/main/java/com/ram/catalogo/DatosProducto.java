package com.ram.catalogo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lo que se ve de un producto desde afuera del módulo. Es una foto de solo lectura.
 *
 * @param precio         precio de lista, en pesos; null = "precio a consultar"
 * @param precioOferta   precio de oferta cargado (puede estar vencida: mirar precioVigente)
 * @param ofertaHasta    último día de la oferta (fecha de Argentina), o null
 * @param precioVigente  lo que se cobra HOY: la oferta si está vigente, si no el de lista. Es el que usan la tienda y los pedidos.
 * @param imagen         imagen "heredada" (una ruta de la web, de los datos de ejemplo), o null
 * @param fotos          las fotos subidas desde el sistema, en orden (la primera es la principal)
 * @param garantiaMeses  null si no tiene garantía informada
 * @param tecnicos       solo en componentes de Armá tu PC; null en el resto
 */
public record DatosProducto(
        ProductoId id,
        String slug,
        String nombre,
        DatosCategoria categoria,
        Condicion condicion,
        Long precio,
        Long precioOferta,
        LocalDate ofertaHasta,
        Long precioVigente,
        String imagen,
        List<UUID> fotos,
        String resumen,
        String descripcion,
        List<String> caracteristicas,
        Integer garantiaMeses,
        boolean publicado,
        DatosTecnicos tecnicos) {

    public DatosProducto {
        fotos = fotos == null ? List.of() : List.copyOf(fotos);
        caracteristicas = caracteristicas == null ? List.of() : List.copyOf(caracteristicas);
    }

    /** Sin oferta ni fotos (para tests y datos simples). */
    public DatosProducto(ProductoId id, String slug, String nombre, DatosCategoria categoria, Condicion condicion, Long precio,
                         String imagen, String resumen, String descripcion, List<String> caracteristicas,
                         Integer garantiaMeses, boolean publicado, DatosTecnicos tecnicos) {
        this(id, slug, nombre, categoria, condicion, precio, null, null, precio, imagen, List.of(), resumen, descripcion,
                caracteristicas, garantiaMeses, publicado, tecnicos);
    }

    public Optional<Long> precioOpcional() {
        return Optional.ofNullable(precioVigente);
    }

    /** Si hoy se vende más barato que el precio de lista. */
    public boolean enOferta() {
        return precio != null && precioVigente != null && precioVigente < precio;
    }

    public boolean esComponente() {
        return tecnicos != null;
    }
}
