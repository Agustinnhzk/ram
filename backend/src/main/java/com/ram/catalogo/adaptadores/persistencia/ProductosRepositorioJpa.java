package com.ram.catalogo.adaptadores.persistencia;

import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.aplicacion.ProductosRepositorio;
import com.ram.catalogo.dominio.Categoria;
import com.ram.catalogo.dominio.Pesos;
import com.ram.catalogo.dominio.Producto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
class ProductosRepositorioJpa implements ProductosRepositorio {

    private final ProductoJpaRepository productos;
    private final CategoriaJpaRepository categorias;

    ProductosRepositorioJpa(ProductoJpaRepository productos, CategoriaJpaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Override
    public List<Categoria> categorias() {
        return categorias.todas().stream().map(ProductosRepositorioJpa::aDominio).toList();
    }

    @Override
    public List<Producto> todos() {
        return productos.todos().stream().map(ProductosRepositorioJpa::aDominio).toList();
    }

    @Override
    public Optional<Producto> porId(ProductoId id) {
        return productos.buscarPorId(id.valor()).map(ProductosRepositorioJpa::aDominio);
    }

    @Override
    public Optional<Producto> porSlug(String slug) {
        return productos.buscarPorSlug(slug).map(ProductosRepositorioJpa::aDominio);
    }

    /**
     * Si existe (en ESTE taller: la búsqueda pasa por el filtro), se actualiza; si no, se crea al final de su categoría.
     * La categoría se busca también filtrada por taller: no se puede asignar una categoría de otro taller.
     */
    @Override
    public void guardar(Producto p) {
        CategoriaJpaEntity categoria = categorias.buscarPorId(p.categoria().id())
                .orElseThrow(() -> new IllegalStateException("La categoría " + p.categoria().slug() + " no existe en este taller"));
        ProductoJpaEntity entidad = productos.buscarPorId(p.id().valor())
                .orElseGet(() -> new ProductoJpaEntity(p.id().valor(), p.slug(), productos.ultimoOrden(categoria.getId()) + 1));
        DatosTecnicos t = p.tecnicos().orElse(null);
        entidad.actualizar(categoria, p.nombre(), p.condicion(),
                p.precio().map(Pesos::valor).orElse(null),
                p.precioOferta().map(Pesos::valor).orElse(null),
                p.ofertaHasta(),
                p.resumen(), p.descripcion(), p.caracteristicas().toArray(String[]::new), p.garantiaMeses(), p.publicado(),
                t == null ? null : t.tipo(),
                t == null ? null : t.socket(),
                t == null ? null : t.tipoRam(),
                t == null ? null : t.formato(),
                t == null ? new String[0] : t.formatosSoportados().stream().map(Enum::name).toArray(String[]::new),
                t == null ? null : t.consumoW(),
                t == null ? null : t.potenciaW(),
                t != null && t.graficosIntegrados());
        productos.save(entidad);
    }

    @Override
    public boolean existeSlug(String slug) {
        return productos.existeSlug(slug);
    }

    @Override
    public Optional<Categoria> categoriaPorSlug(String slug) {
        return categorias.buscarPorSlug(slug).map(ProductosRepositorioJpa::aDominio);
    }

    @Override
    public boolean existeSlugDeCategoria(String slug) {
        return categorias.buscarPorSlug(slug).isPresent();
    }

    @Override
    public void guardarCategoria(Categoria c) {
        CategoriaJpaEntity entidad = categorias.buscarPorId(c.id()).orElseGet(() -> {
            Integer ultimo = categorias.ultimoOrden();
            return new CategoriaJpaEntity(c.id(), c.slug(), ultimo == null ? 1 : ultimo + 1);
        });
        entidad.actualizar(c.nombre(), c.conInstalacion());
        categorias.save(entidad);
    }

    private static Categoria aDominio(CategoriaJpaEntity c) {
        return new Categoria(c.getId(), c.getSlug(), c.getNombre(), c.isConInstalacion());
    }

    private static Producto aDominio(ProductoJpaEntity e) {
        DatosTecnicos tecnicos = e.getTipoComponente() == null ? null : new DatosTecnicos(
                e.getTipoComponente(),
                e.getSocket(),
                e.getTipoRam(),
                e.getFormato(),
                Arrays.stream(e.getFormatosSoportados()).map(FormatoMother::valueOf).toList(),
                e.getConsumoW(),
                e.getPotenciaW(),
                e.isGraficosIntegrados());

        return new Producto(
                ProductoId.de(e.getId()),
                e.getSlug(),
                e.getNombre(),
                aDominio(e.getCategoria()),
                e.getCondicion(),
                e.getPrecio() == null ? null : new Pesos(e.getPrecio()),
                e.getPrecioOferta() == null ? null : new Pesos(e.getPrecioOferta()),
                e.getOfertaHasta(),
                e.getImagen(),
                e.getResumen(),
                e.getDescripcion(),
                Arrays.asList(e.getCaracteristicas()),
                e.getGarantiaMeses(),
                e.isPublicado(),
                tecnicos);
    }
}
