package com.ram.catalogo.aplicacion;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.dominio.CambioDePrecio;
import com.ram.catalogo.dominio.Categoria;
import com.ram.catalogo.dominio.Producto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio, historial y fotos en memoria, para testear los casos de uso sin base de datos. */
class CatalogoEnMemoria implements ProductosRepositorio, HistorialDePrecios, FotosDeProductos {

    private final List<Categoria> categorias = new ArrayList<>();
    private final Map<ProductoId, Producto> productos = new LinkedHashMap<>();
    private final List<CambioDePrecio> cambios = new ArrayList<>();

    void agregar(Categoria categoria) {
        categorias.add(categoria);
    }

    void agregar(Producto producto) {
        productos.put(producto.id(), producto);
    }

    List<CambioDePrecio> cambios() {
        return cambios;
    }

    @Override
    public List<Categoria> categorias() {
        return List.copyOf(categorias);
    }

    @Override
    public List<Producto> todos() {
        return List.copyOf(productos.values());
    }

    @Override
    public Optional<Producto> porId(ProductoId id) {
        return Optional.ofNullable(productos.get(id));
    }

    @Override
    public Optional<Producto> porSlug(String slug) {
        return productos.values().stream().filter(p -> p.slug().equals(slug)).findFirst();
    }

    @Override
    public void guardar(Producto producto) {
        if (categorias.stream().noneMatch(c -> c.id().equals(producto.categoria().id()))) {
            throw new IllegalStateException("No existe la categoría " + producto.categoria().slug());
        }
        productos.put(producto.id(), producto);
    }

    @Override
    public boolean existeSlug(String slug) {
        return porSlug(slug).isPresent();
    }

    @Override
    public Optional<Categoria> categoriaPorSlug(String slug) {
        return categorias.stream().filter(c -> c.slug().equals(slug)).findFirst();
    }

    @Override
    public boolean existeSlugDeCategoria(String slug) {
        return categoriaPorSlug(slug).isPresent();
    }

    @Override
    public void guardarCategoria(Categoria categoria) {
        categorias.removeIf(c -> c.id().equals(categoria.id()));
        categorias.add(categoria);
    }

    // ─── Fotos ────────────────────────────────────────────────────────────────────────────────

    final Map<ProductoId, List<java.util.UUID>> fotos = new LinkedHashMap<>();
    final Map<java.util.UUID, byte[][]> archivos = new LinkedHashMap<>();

    @Override
    public List<java.util.UUID> de(ProductoId producto) {
        return List.copyOf(fotos.getOrDefault(producto, List.of()));
    }

    @Override
    public Map<ProductoId, List<java.util.UUID>> deTodos() {
        return Map.copyOf(fotos);
    }

    @Override
    public java.util.UUID agregar(ProductoId producto, com.ram.archivos.ImagenLista grande, com.ram.archivos.ImagenLista chica) {
        java.util.UUID id = java.util.UUID.randomUUID();
        fotos.computeIfAbsent(producto, k -> new ArrayList<>()).add(id);
        archivos.put(id, new byte[][]{grande.jpeg(), chica.jpeg()});
        return id;
    }

    @Override
    public void borrar(java.util.UUID foto) {
        fotos.values().forEach(l -> l.remove(foto));
        archivos.remove(foto);
    }

    @Override
    public void ordenar(ProductoId producto, List<java.util.UUID> orden) {
        fotos.put(producto, new ArrayList<>(orden));
    }

    @Override
    public Optional<byte[]> archivo(java.util.UUID foto, boolean chica) {
        return Optional.ofNullable(archivos.get(foto)).map(a -> chica ? a[1] : a[0]);
    }

    @Override
    public void registrar(CambioDePrecio cambio) {
        cambios.add(cambio);
    }

    @Override
    public List<CambioDePrecio> deProducto(ProductoId producto, int limite) {
        return cambios.stream()
                .filter(c -> c.producto().equals(producto))
                .sorted(Comparator.comparing(CambioDePrecio::cuando).reversed())
                .limit(limite)
                .toList();
    }
}
