package com.ram.catalogo;

import com.ram.archivos.ArchivoSubido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada del módulo: todo lo que el resto del sistema puede hacer con el catálogo.
 * Siempre trabaja sobre el taller de la request actual.
 */
public interface Catalogo {

    // ─── Para la tienda (público) ──────────────────────────────────────────────────────────────

    List<DatosCategoria> categorias();

    /** Productos publicados que cumplen los filtros, en el orden en que se muestran. */
    List<DatosProducto> buscar(FiltroCatalogo filtro);

    /** Un producto publicado. Vacío si no existe o no está publicado. */
    Optional<DatosProducto> obtenerPublicado(String slug);

    /** Todos los componentes de Armá tu PC con sus datos técnicos (publicados o no como productos sueltos). */
    List<DatosProducto> componentes();

    // ─── Para el sistema de gestión (personal logueado) ───────────────────────────────────────

    /** Todos los productos, publicados o no, para la página de precios. */
    List<DatosProducto> listarParaPrecios();

    /**
     * Cambia el precio de un producto y lo registra en el historial. Si es el mismo precio, no hace nada.
     *
     * @param nuevoPrecio en pesos; null = pasa a "precio a consultar"
     * @throws SinPermisoException           si quien lo pide no es Dueño
     * @throws ProductoNoEncontradoException si no existe en este taller
     * @throws IllegalArgumentException      si el precio es negativo o absurdo
     */
    void cambiarPrecio(ProductoId id, Long nuevoPrecio);

    /**
     * Calcula cómo quedarían los precios con un aumento, sin cambiar nada.
     *
     * @param porcentaje por ejemplo 8 (= +8%) o -10 (= rebaja del 10%)
     * @param categoria  slug de la categoría, o null para todos
     */
    VistaPreviaDeAumento simularAumento(BigDecimal porcentaje, String categoria);

    /**
     * Aplica el aumento y lo registra en el historial de cada producto. Los que no tienen precio no se tocan.
     *
     * @return cuántos productos cambiaron de precio
     * @throws SinPermisoException si quien lo pide no es Dueño
     */
    int aplicarAumento(BigDecimal porcentaje, String categoria);

    /** Los últimos cambios de precio de un producto, del más nuevo al más viejo. */
    List<DatosCambioDePrecio> historialDePrecios(ProductoId id);

    // ─── Carga de productos (ADR-013). Todo lo que modifica: solo el Dueño ─────────────────────

    /** Un producto, publicado o no (para editarlo). */
    Optional<DatosProducto> obtener(ProductoId id);

    /**
     * @param precio en pesos; null = "a consultar". Queda en el historial como primer precio.
     * @throws IllegalArgumentException si la ficha no es válida (el mensaje se puede mostrar)
     */
    ProductoId crearProducto(FichaDeProducto ficha, Long precio);

    /** El slug (la dirección del producto) no cambia aunque cambie el nombre: los links viejos siguen andando. */
    void editarProducto(ProductoId id, FichaDeProducto ficha);

    /**
     * Pone un precio de oferta hasta una fecha (inclusive, fecha de Argentina). Tiene que ser menor al precio de lista.
     * Con los dos en null, quita la oferta.
     */
    void cambiarOferta(ProductoId id, Long precioOferta, LocalDate hasta);

    /** Achica la foto, la endereza y la guarda al final. Hasta 6 por producto. */
    UUID agregarFoto(ProductoId id, ArchivoSubido imagen);

    void borrarFoto(ProductoId id, UUID foto);

    /** El nuevo orden: tienen que estar todas las fotos del producto, una vez cada una. La primera es la principal. */
    void ordenarFotos(ProductoId id, List<UUID> fotos);

    /** Para la tienda (público). @param chica la versión para tarjetas (600 px) en vez de la grande (1600 px) */
    Optional<ArchivoDeFoto> foto(UUID foto, boolean chica);

    DatosCategoria crearCategoria(String nombre, boolean conInstalacion);

    /** El slug no cambia (está en las direcciones de la tienda). */
    void editarCategoria(String slug, String nombre, boolean conInstalacion);
}
