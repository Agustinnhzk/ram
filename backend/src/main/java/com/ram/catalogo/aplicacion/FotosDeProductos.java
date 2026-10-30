package com.ram.catalogo.aplicacion;

import com.ram.archivos.ImagenLista;
import com.ram.catalogo.ProductoId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: dónde viven las fotos de los productos (en la base, ADR-013). */
public interface FotosDeProductos {

    /** Las fotos de un producto, en orden. */
    List<UUID> de(ProductoId producto);

    /** Las de todos los productos del taller en una sola consulta (para el catálogo). */
    Map<ProductoId, List<UUID>> deTodos();

    /** La agrega al final. */
    UUID agregar(ProductoId producto, ImagenLista grande, ImagenLista chica);

    void borrar(UUID foto);

    /** Guarda el orden (la lista tiene exactamente las fotos del producto). */
    void ordenar(ProductoId producto, List<UUID> fotos);

    Optional<byte[]> archivo(UUID foto, boolean chica);
}
