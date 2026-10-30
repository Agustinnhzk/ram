package com.ram.catalogo.aplicacion;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.dominio.Categoria;
import com.ram.catalogo.dominio.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida. Todo es, implícitamente, del taller actual.
 *
 * <p>Los filtros de la tienda (categoría, estado, búsqueda) los aplica el servicio sobre
 * {@link #todos()}: un taller tiene cientos de productos, no millones, y así las reglas de búsqueda
 * quedan en un solo lugar y se pueden testear sin base de datos. Si algún día son miles, se pasan a SQL.
 */
public interface ProductosRepositorio {

    /** Ordenadas como se muestran en la tienda. */
    List<Categoria> categorias();

    /** Todos los productos del taller, publicados o no, en el orden en que se muestran. */
    List<Producto> todos();

    Optional<Producto> porId(ProductoId id);

    Optional<Producto> porSlug(String slug);

    /** Guarda un producto nuevo o los cambios de uno que ya existe. */
    void guardar(Producto producto);

    /** Si el slug ya lo usa otro producto del taller. */
    boolean existeSlug(String slug);

    Optional<Categoria> categoriaPorSlug(String slug);

    boolean existeSlugDeCategoria(String slug);

    /** Una categoría nueva va al final; una existente cambia nombre e instalación. */
    void guardarCategoria(Categoria categoria);
}
