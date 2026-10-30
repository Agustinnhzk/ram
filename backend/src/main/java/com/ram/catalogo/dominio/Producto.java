package com.ram.catalogo.dominio;

import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.ProductoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Un producto del catálogo. Si tiene datos técnicos, además es un componente de Armá tu PC.
 * Igual que Cliente, no sabe de qué taller es: eso lo resuelve la persistencia (ADR-005).
 *
 * <p>Precios: el de lista ({@link #precio()}) y, opcionalmente, uno de oferta hasta una fecha.
 * Lo que se cobra hoy lo dice {@link #precioVigente(LocalDate)} (ADR-013).
 */
public class Producto {

    static final int CARACTERISTICAS_MAXIMAS = 20;

    private final ProductoId id;
    private final String slug;
    private String nombre;
    private Categoria categoria;
    private Condicion condicion;
    private Pesos precio;
    private Pesos precioOferta;
    private LocalDate ofertaHasta;
    private final String imagen;
    private String resumen;
    private String descripcion;
    private List<String> caracteristicas;
    private Integer garantiaMeses;
    private boolean publicado;
    private DatosTecnicos tecnicos;

    /** Para reconstruir uno guardado, o para tests (sin validar). */
    public Producto(ProductoId id, String slug, String nombre, Categoria categoria, Condicion condicion, Pesos precio,
                    Pesos precioOferta, LocalDate ofertaHasta, String imagen, String resumen, String descripcion,
                    List<String> caracteristicas, Integer garantiaMeses, boolean publicado, DatosTecnicos tecnicos) {
        this.id = Objects.requireNonNull(id);
        this.slug = Objects.requireNonNull(slug);
        this.nombre = Objects.requireNonNull(nombre);
        this.categoria = Objects.requireNonNull(categoria);
        this.condicion = Objects.requireNonNull(condicion);
        this.precio = precio;
        this.precioOferta = precioOferta;
        this.ofertaHasta = ofertaHasta;
        this.imagen = imagen;
        this.resumen = resumen == null ? "" : resumen;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.caracteristicas = caracteristicas == null ? List.of() : List.copyOf(caracteristicas);
        this.garantiaMeses = garantiaMeses;
        this.publicado = publicado;
        this.tecnicos = tecnicos;
    }

    /** Sin oferta. */
    public Producto(ProductoId id, String slug, String nombre, Categoria categoria, Condicion condicion, Pesos precio,
                    String imagen, String resumen, String descripcion, List<String> caracteristicas,
                    Integer garantiaMeses, boolean publicado, DatosTecnicos tecnicos) {
        this(id, slug, nombre, categoria, condicion, precio, null, null, imagen, resumen, descripcion, caracteristicas,
                garantiaMeses, publicado, tecnicos);
    }

    /** Un producto nuevo, cargado desde el sistema. Valida todo. */
    public static Producto crear(String slug, Categoria categoria, Ficha ficha, Pesos precio) {
        Producto nuevo = new Producto(ProductoId.nuevo(), slug, "-", categoria, Condicion.NUEVO, precio,
                null, "", "", List.of(), null, false, null);
        nuevo.editar(categoria, ficha);
        return nuevo;
    }

    /** Lo que se edita en el formulario, ya con la categoría encontrada. */
    public record Ficha(String nombre, Condicion condicion, String resumen, String descripcion, List<String> caracteristicas,
                        Integer garantiaMeses, boolean publicado, DatosTecnicos tecnicos) {
    }

    public void editar(Categoria nuevaCategoria, Ficha ficha) {
        this.nombre = Texto.obligatorio(ficha.nombre(), "el nombre", 150);
        this.categoria = Objects.requireNonNull(nuevaCategoria);
        if (ficha.condicion() == null) {
            throw new IllegalArgumentException("Falta la condición (nuevo o reacondicionado)");
        }
        this.condicion = ficha.condicion();
        this.resumen = Texto.opcional(ficha.resumen(), "el resumen", 300);
        this.descripcion = Texto.opcional(ficha.descripcion(), "la descripción", 5000);
        this.caracteristicas = caracteristicas(ficha.caracteristicas());
        if (ficha.garantiaMeses() != null && (ficha.garantiaMeses() < 1 || ficha.garantiaMeses() > 120)) {
            throw new IllegalArgumentException("La garantía tiene que ser de 1 a 120 meses");
        }
        this.garantiaMeses = ficha.garantiaMeses();
        this.publicado = ficha.publicado();
        this.tecnicos = DatosTecnicosValidos.revisar(ficha.tecnicos());
    }

    /**
     * @param nuevo null = pasa a "precio a consultar"
     * @return true si el precio cambió (si es el mismo, no hay nada que registrar)
     */
    public boolean cambiarPrecio(Pesos nuevo) {
        if (Objects.equals(precio, nuevo)) {
            return false;
        }
        this.precio = nuevo;
        return true;
    }

    /** @param hoy la fecha de hoy en Argentina: la oferta no puede terminar en el pasado */
    public void ponerOferta(Pesos oferta, LocalDate hasta, LocalDate hoy) {
        Objects.requireNonNull(oferta, "Falta el precio de oferta");
        if (hasta == null) {
            throw new IllegalArgumentException("Falta hasta cuándo dura la oferta");
        }
        if (precio == null) {
            throw new IllegalArgumentException("Para poner una oferta, el producto tiene que tener precio de lista");
        }
        if (oferta.valor() <= 0 || oferta.valor() >= precio.valor()) {
            throw new IllegalArgumentException("El precio de oferta tiene que ser menor al de lista");
        }
        if (hasta.isBefore(hoy)) {
            throw new IllegalArgumentException("La oferta no puede terminar antes de hoy");
        }
        this.precioOferta = oferta;
        this.ofertaHasta = hasta;
    }

    public void quitarOferta() {
        this.precioOferta = null;
        this.ofertaHasta = null;
    }

    /**
     * Lo que se cobra hoy: la oferta si está cargada, no venció y sigue siendo menor que el precio de lista
     * (si después de cargarla se bajó el precio de lista por debajo, gana el más barato).
     */
    public Optional<Pesos> precioVigente(LocalDate hoy) {
        if (ofertaVigente(hoy)) {
            return Optional.of(precioOferta);
        }
        return Optional.ofNullable(precio);
    }

    public boolean ofertaVigente(LocalDate hoy) {
        return precioOferta != null && precio != null && !hoy.isAfter(ofertaHasta) && precioOferta.valor() < precio.valor();
    }

    /** Todo el texto por el que se puede encontrar este producto en el buscador, ya normalizado. */
    public String textoParaBuscar() {
        return TextoDeBusqueda.normalizar(String.join(" ", nombre, resumen, String.join(" ", caracteristicas), categoria.nombre()));
    }

    private static List<String> caracteristicas(List<String> lista) {
        if (lista == null) {
            return List.of();
        }
        List<String> limpias = lista.stream().filter(c -> c != null && !c.isBlank()).map(String::strip).toList();
        if (limpias.size() > CARACTERISTICAS_MAXIMAS) {
            throw new IllegalArgumentException("Hasta " + CARACTERISTICAS_MAXIMAS + " características");
        }
        if (limpias.stream().anyMatch(c -> c.length() > 120)) {
            throw new IllegalArgumentException("Cada característica puede tener hasta 120 caracteres");
        }
        return limpias;
    }

    public ProductoId id() {
        return id;
    }

    public String slug() {
        return slug;
    }

    public String nombre() {
        return nombre;
    }

    public Categoria categoria() {
        return categoria;
    }

    public Condicion condicion() {
        return condicion;
    }

    public Optional<Pesos> precio() {
        return Optional.ofNullable(precio);
    }

    public Optional<Pesos> precioOferta() {
        return Optional.ofNullable(precioOferta);
    }

    public LocalDate ofertaHasta() {
        return ofertaHasta;
    }

    public String imagen() {
        return imagen;
    }

    public String resumen() {
        return resumen;
    }

    public String descripcion() {
        return descripcion;
    }

    public List<String> caracteristicas() {
        return caracteristicas;
    }

    public Integer garantiaMeses() {
        return garantiaMeses;
    }

    public boolean publicado() {
        return publicado;
    }

    public Optional<DatosTecnicos> tecnicos() {
        return Optional.ofNullable(tecnicos);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Producto otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
