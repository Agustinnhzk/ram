package com.ram.catalogo.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.ImagenLista;
import com.ram.archivos.Imagenes;
import com.ram.catalogo.ArchivoDeFoto;
import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.FichaDeProducto;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.MotivoDeCambio;
import com.ram.catalogo.PrecioPropuesto;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.SinPermisoException;
import com.ram.catalogo.VistaPreviaDeAumento;
import com.ram.catalogo.dominio.CambioDePrecio;
import com.ram.catalogo.dominio.Categoria;
import com.ram.catalogo.dominio.Pesos;
import com.ram.catalogo.dominio.Porcentaje;
import com.ram.catalogo.dominio.Producto;
import com.ram.catalogo.dominio.Slug;
import com.ram.catalogo.dominio.TextoDeBusqueda;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Casos de uso del catálogo. Implementa la API pública del módulo.
 *
 * <p>Quién puede qué:
 * <ul>
 *   <li>Consultar el catálogo: cualquiera (la tienda es pública).</li>
 *   <li>Ver precios e historial, simular aumentos: personal logueado (lo controla la seguridad web).</li>
 *   <li>Cambiar precios, ofertas, productos, fotos y categorías: solo el Dueño (regla del negocio, la controla este servicio).</li>
 * </ul>
 */
public class ServicioCatalogo implements Catalogo {

    static final int LIMITE_HISTORIAL = 50;
    static final int FOTOS_MAXIMAS = 6;
    static final int LADO_GRANDE = 1600;
    static final int LADO_CHICA = 600;

    private final ProductosRepositorio productos;
    private final HistorialDePrecios historial;
    private final FotosDeProductos fotos;
    private final SesionActual sesion;
    private final Clock reloj;
    private final ZoneId zona;

    public ServicioCatalogo(ProductosRepositorio productos, HistorialDePrecios historial, FotosDeProductos fotos,
                            SesionActual sesion, Clock reloj, ZoneId zona) {
        this.productos = Objects.requireNonNull(productos);
        this.historial = Objects.requireNonNull(historial);
        this.fotos = Objects.requireNonNull(fotos);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
        this.zona = Objects.requireNonNull(zona);
    }

    // ─── Tienda ───────────────────────────────────────────────────────────────────────────────

    @Override
    public List<DatosCategoria> categorias() {
        return productos.categorias().stream().map(ServicioCatalogo::aDatos).toList();
    }

    @Override
    public List<DatosProducto> buscar(FiltroCatalogo filtro) {
        List<String> palabras = TextoDeBusqueda.palabras(filtro.texto());
        Map<ProductoId, List<UUID>> todasLasFotos = fotos.deTodos();
        LocalDate hoy = hoy();
        return productos.todos().stream()
                .filter(Producto::publicado)
                .filter(p -> filtro.categoria() == null || p.categoria().slug().equals(filtro.categoria()))
                .filter(p -> filtro.condicion() == null || p.condicion() == filtro.condicion())
                // Todas las palabras tienen que aparecer: "kit dvr" encuentra "Kit 4 cámaras + DVR".
                .filter(p -> palabras.stream().allMatch(p.textoParaBuscar()::contains))
                .map(p -> aDatos(p, todasLasFotos.getOrDefault(p.id(), List.of()), hoy))
                .toList();
    }

    @Override
    public Optional<DatosProducto> obtenerPublicado(String slug) {
        return productos.porSlug(slug).filter(Producto::publicado).map(this::conFotos);
    }

    @Override
    public List<DatosProducto> componentes() {
        Map<ProductoId, List<UUID>> todasLasFotos = fotos.deTodos();
        LocalDate hoy = hoy();
        return productos.todos().stream()
                .filter(p -> p.tecnicos().isPresent())
                .map(p -> aDatos(p, todasLasFotos.getOrDefault(p.id(), List.of()), hoy))
                .toList();
    }

    @Override
    public Optional<ArchivoDeFoto> foto(UUID foto, boolean chica) {
        return fotos.archivo(foto, chica).map(ArchivoDeFoto::new);
    }

    // ─── Sistema de gestión: precios ─────────────────────────────────────────────────────────

    @Override
    public List<DatosProducto> listarParaPrecios() {
        Map<ProductoId, List<UUID>> todasLasFotos = fotos.deTodos();
        LocalDate hoy = hoy();
        return productos.todos().stream().map(p -> aDatos(p, todasLasFotos.getOrDefault(p.id(), List.of()), hoy)).toList();
    }

    @Override
    public void cambiarPrecio(ProductoId id, Long nuevoPrecio) {
        UsuarioAutenticado usuario = exigirDueno();
        Producto producto = buscar(id);
        Pesos nuevo = nuevoPrecio == null ? null : new Pesos(nuevoPrecio);

        Pesos anterior = producto.precio().orElse(null);
        if (producto.cambiarPrecio(nuevo)) {
            productos.guardar(producto);
            historial.registrar(new CambioDePrecio(
                    id, anterior, nuevo, MotivoDeCambio.MANUAL, null, usuario.usuarioId(), usuario.nombre(), reloj.instant()));
        }
    }

    @Override
    public VistaPreviaDeAumento simularAumento(BigDecimal porcentaje, String categoria) {
        Porcentaje aplicado = new Porcentaje(porcentaje);
        List<PrecioPropuesto> precios = afectados(categoria).stream()
                .map(p -> {
                    Pesos actual = p.precio().orElseThrow();
                    return new PrecioPropuesto(p.id(), p.nombre(), p.categoria().nombre(), actual.valor(), actual.aplicar(aplicado).valor());
                })
                .toList();
        return new VistaPreviaDeAumento(porcentaje, categoria, precios);
    }

    /** El aumento cambia el precio de LISTA. Las ofertas cargadas no se tocan: tienen su propio precio y su fecha. */
    @Override
    public int aplicarAumento(BigDecimal porcentaje, String categoria) {
        UsuarioAutenticado usuario = exigirDueno();
        Porcentaje aplicado = new Porcentaje(porcentaje);
        int cambiados = 0;
        for (Producto producto : afectados(categoria)) {
            Pesos anterior = producto.precio().orElseThrow();
            Pesos nuevo = anterior.aplicar(aplicado);
            // Puede no cambiar: un precio muy chico con un porcentaje chico redondea al mismo valor.
            if (producto.cambiarPrecio(nuevo)) {
                productos.guardar(producto);
                historial.registrar(new CambioDePrecio(producto.id(), anterior, nuevo, MotivoDeCambio.PORCENTAJE,
                        aplicado.valor(), usuario.usuarioId(), usuario.nombre(), reloj.instant()));
                cambiados++;
            }
        }
        return cambiados;
    }

    @Override
    public List<DatosCambioDePrecio> historialDePrecios(ProductoId id) {
        buscar(id);
        return historial.deProducto(id, LIMITE_HISTORIAL).stream()
                .map(c -> new DatosCambioDePrecio(
                        c.anterior() == null ? null : c.anterior().valor(),
                        c.nuevo() == null ? null : c.nuevo().valor(),
                        c.motivo(),
                        c.porcentaje(),
                        c.nombreUsuario(),
                        c.cuando()))
                .toList();
    }

    // ─── Sistema de gestión: carga de productos (ADR-013) ────────────────────────────────────

    @Override
    public Optional<DatosProducto> obtener(ProductoId id) {
        return productos.porId(id).map(this::conFotos);
    }

    @Override
    public ProductoId crearProducto(FichaDeProducto ficha, Long precio) {
        UsuarioAutenticado usuario = exigirDueno();
        Categoria categoria = categoria(ficha.categoria());
        String nombre = ficha.nombre() == null ? "" : ficha.nombre();
        Pesos inicial = precio == null ? null : new Pesos(precio);
        Producto nuevo = Producto.crear(Slug.libre(nombre, productos::existeSlug), categoria, aFicha(ficha), inicial);
        productos.guardar(nuevo);
        if (inicial != null) {
            historial.registrar(new CambioDePrecio(nuevo.id(), null, inicial, MotivoDeCambio.MANUAL, null,
                    usuario.usuarioId(), usuario.nombre(), reloj.instant()));
        }
        return nuevo.id();
    }

    @Override
    public void editarProducto(ProductoId id, FichaDeProducto ficha) {
        exigirDueno();
        Producto producto = buscar(id);
        producto.editar(categoria(ficha.categoria()), aFicha(ficha));
        productos.guardar(producto);
    }

    @Override
    public void cambiarOferta(ProductoId id, Long precioOferta, LocalDate hasta) {
        exigirDueno();
        Producto producto = buscar(id);
        if (precioOferta == null && hasta == null) {
            producto.quitarOferta();
        } else {
            producto.ponerOferta(precioOferta == null ? null : new Pesos(precioOferta), hasta, hoy());
        }
        productos.guardar(producto);
    }

    @Override
    public UUID agregarFoto(ProductoId id, ArchivoSubido imagen) {
        exigirDueno();
        buscar(id);
        if (fotos.de(id).size() >= FOTOS_MAXIMAS) {
            throw new IllegalArgumentException("Cada producto puede tener hasta " + FOTOS_MAXIMAS + " fotos");
        }
        ImagenLista grande = Imagenes.preparar(imagen, LADO_GRANDE);
        ImagenLista chica = Imagenes.preparar(imagen, LADO_CHICA);
        return fotos.agregar(id, grande, chica);
    }

    @Override
    public void borrarFoto(ProductoId id, UUID foto) {
        exigirDueno();
        if (!fotos.de(id).contains(foto)) {
            throw new ProductoNoEncontradoException(id);
        }
        fotos.borrar(foto);
    }

    @Override
    public void ordenarFotos(ProductoId id, List<UUID> nuevoOrden) {
        exigirDueno();
        List<UUID> actuales = fotos.de(id);
        if (nuevoOrden == null || nuevoOrden.size() != actuales.size() || !new HashSet<>(nuevoOrden).equals(new HashSet<>(actuales))) {
            throw new IllegalArgumentException("El orden tiene que incluir todas las fotos del producto, una vez cada una");
        }
        fotos.ordenar(id, nuevoOrden);
    }

    @Override
    public DatosCategoria crearCategoria(String nombre, boolean conInstalacion) {
        exigirDueno();
        String limpio = nombreDeCategoria(nombre);
        Categoria nueva = new Categoria(UUID.randomUUID(), Slug.libre(limpio, productos::existeSlugDeCategoria), limpio, conInstalacion);
        productos.guardarCategoria(nueva);
        return aDatos(nueva);
    }

    @Override
    public void editarCategoria(String slug, String nombre, boolean conInstalacion) {
        exigirDueno();
        Categoria actual = categoria(slug);
        productos.guardarCategoria(new Categoria(actual.id(), actual.slug(), nombreDeCategoria(nombre), conInstalacion));
    }

    // ─── Ayudas ──────────────────────────────────────────────────────────────────────────────

    /** Los productos a los que les llega un aumento: los que tienen precio, de la categoría pedida (o todos). */
    private List<Producto> afectados(String categoria) {
        if (categoria != null && productos.categorias().stream().noneMatch(c -> c.slug().equals(categoria))) {
            throw new IllegalArgumentException("No existe la categoría " + categoria);
        }
        return productos.todos().stream()
                .filter(p -> p.precio().isPresent())
                .filter(p -> categoria == null || p.categoria().slug().equals(categoria))
                .toList();
    }

    private Producto buscar(ProductoId id) {
        return productos.porId(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    private Categoria categoria(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Elegí una categoría");
        }
        return productos.categoriaPorSlug(slug).orElseThrow(() -> new IllegalArgumentException("No existe la categoría " + slug));
    }

    private static String nombreDeCategoria(String nombre) {
        String limpio = nombre == null ? "" : nombre.strip();
        if (limpio.isEmpty() || limpio.length() > 80) {
            throw new IllegalArgumentException("El nombre de la categoría tiene que tener entre 1 y 80 caracteres");
        }
        return limpio;
    }

    private static Producto.Ficha aFicha(FichaDeProducto f) {
        return new Producto.Ficha(f.nombre(), f.condicion(), f.resumen(), f.descripcion(), f.caracteristicas(),
                f.garantiaMeses(), f.publicado(), f.tecnicos());
    }

    private UsuarioAutenticado exigirDueno() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (!usuario.tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede cambiar el catálogo y los precios");
        }
        return usuario;
    }

    /** La fecha de hoy en el taller: decide si una oferta sigue vigente. */
    private LocalDate hoy() {
        return LocalDate.ofInstant(reloj.instant(), zona);
    }

    private DatosProducto conFotos(Producto p) {
        return aDatos(p, fotos.de(p.id()), hoy());
    }

    private static DatosCategoria aDatos(Categoria c) {
        return new DatosCategoria(c.slug(), c.nombre(), c.conInstalacion());
    }

    private static DatosProducto aDatos(Producto p, List<UUID> fotosDelProducto, LocalDate hoy) {
        return new DatosProducto(
                p.id(),
                p.slug(),
                p.nombre(),
                aDatos(p.categoria()),
                p.condicion(),
                p.precio().map(Pesos::valor).orElse(null),
                p.precioOferta().map(Pesos::valor).orElse(null),
                p.ofertaHasta(),
                p.precioVigente(hoy).map(Pesos::valor).orElse(null),
                p.imagen(),
                fotosDelProducto,
                p.resumen(),
                p.descripcion(),
                p.caracteristicas(),
                p.garantiaMeses(),
                p.publicado(),
                p.tecnicos().orElse(null));
    }
}
