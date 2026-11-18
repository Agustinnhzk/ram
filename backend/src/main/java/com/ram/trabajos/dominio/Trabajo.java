package com.ram.trabajos.dominio;

import com.ram.trabajos.TipoDeTrabajo;
import com.ram.trabajos.TrabajoNuevo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/** Un trabajo realizado, con sus reglas. */
public final class Trabajo {

    static final int LARGO_TITULO = 120;
    static final int LARGO_TEXTO = 1000;

    private final UUID id;
    private final String slug;
    private final Instant creadoEn;
    private TipoDeTrabajo tipo;
    private String titulo;
    private Barrio barrio;
    private boolean aDomicilio;
    private String problema;
    private String diagnostico;
    private String solucion;
    private LocalDate fecha;
    private Integer numeroDeOrden;
    private boolean consentimiento;
    private boolean publicado;

    public Trabajo(UUID id, String slug, Instant creadoEn, TipoDeTrabajo tipo, String titulo, Barrio barrio, boolean aDomicilio,
                   String problema, String diagnostico, String solucion, LocalDate fecha, Integer numeroDeOrden,
                   boolean consentimiento, boolean publicado) {
        this.id = Objects.requireNonNull(id);
        this.slug = Objects.requireNonNull(slug);
        this.creadoEn = Objects.requireNonNull(creadoEn);
        this.tipo = tipo;
        this.titulo = titulo;
        this.barrio = barrio;
        this.aDomicilio = aDomicilio;
        this.problema = problema;
        this.diagnostico = diagnostico;
        this.solucion = solucion;
        this.fecha = fecha;
        this.numeroDeOrden = numeroDeOrden;
        this.consentimiento = consentimiento;
        this.publicado = publicado;
    }

    /**
     * El slug sale del título y el barrio ("notebook-que-no-encendia-abasto") y no cambia al editar:
     * así un link que ya se compartió o que Google ya conoce sigue andando.
     */
    public static Trabajo nuevo(TrabajoNuevo datos, LocalDate hoy, Instant ahora, Predicate<String> existeSlug) {
        Trabajo trabajo = new Trabajo(UUID.randomUUID(), "pendiente", ahora, null, null, null, false, null, null, null,
                null, null, false, false);
        trabajo.editar(datos, hoy);
        String slug = Slug.libre(trabajo.titulo + " " + trabajo.barrio.texto(), existeSlug);
        return trabajo.conSlug(slug);
    }

    private Trabajo conSlug(String slug) {
        return new Trabajo(id, slug, creadoEn, tipo, titulo, barrio, aDomicilio, problema, diagnostico, solucion, fecha,
                numeroDeOrden, consentimiento, publicado);
    }

    public void editar(TrabajoNuevo datos, LocalDate hoy) {
        if (datos.tipo() == null) {
            throw new IllegalArgumentException("Elegí qué tipo de trabajo fue");
        }
        String titulo = obligatorio(datos.titulo(), 5, LARGO_TITULO,
                "Escribí un título corto, por ejemplo \"Notebook que no encendía\" (de 5 a " + LARGO_TITULO + " caracteres)");
        Barrio barrio = Barrio.de(datos.barrio());
        String problema = obligatorio(datos.problema(), 3, LARGO_TEXTO, "Contá cuál era el problema (hasta " + LARGO_TEXTO + " caracteres)");
        String solucion = obligatorio(datos.solucion(), 3, LARGO_TEXTO, "Contá qué se hizo (hasta " + LARGO_TEXTO + " caracteres)");
        String diagnostico = opcional(datos.diagnostico(), "El diagnóstico puede tener hasta " + LARGO_TEXTO + " caracteres");
        LocalDate fecha = datos.fecha() == null ? hoy : datos.fecha();
        if (fecha.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha del trabajo no puede ser futura");
        }
        if (datos.numeroDeOrden() != null && datos.numeroDeOrden() <= 0) {
            throw new IllegalArgumentException("El número de orden tiene que ser mayor a cero");
        }
        if (datos.publicado() && !datos.consentimiento()) {
            throw new IllegalArgumentException("Para publicarlo, marcá que el cliente dio permiso");
        }
        this.tipo = datos.tipo();
        this.titulo = titulo;
        this.barrio = barrio;
        this.aDomicilio = datos.aDomicilio();
        this.problema = problema;
        this.diagnostico = diagnostico;
        this.solucion = solucion;
        this.fecha = fecha;
        this.numeroDeOrden = datos.numeroDeOrden();
        this.consentimiento = datos.consentimiento();
        this.publicado = datos.publicado();
    }

    private static String obligatorio(String texto, int minimo, int maximo, String error) {
        String limpio = texto == null ? "" : texto.strip();
        if (limpio.length() < minimo || limpio.length() > maximo) {
            throw new IllegalArgumentException(error);
        }
        return limpio;
    }

    private static String opcional(String texto, String error) {
        String limpio = texto == null ? "" : texto.strip();
        if (limpio.length() > LARGO_TEXTO) {
            throw new IllegalArgumentException(error);
        }
        return limpio.isEmpty() ? null : limpio;
    }

    public UUID id() {
        return id;
    }

    public String slug() {
        return slug;
    }

    public Instant creadoEn() {
        return creadoEn;
    }

    public TipoDeTrabajo tipo() {
        return tipo;
    }

    public String titulo() {
        return titulo;
    }

    public Barrio barrio() {
        return barrio;
    }

    public boolean aDomicilio() {
        return aDomicilio;
    }

    public String problema() {
        return problema;
    }

    public String diagnostico() {
        return diagnostico;
    }

    public String solucion() {
        return solucion;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public Integer numeroDeOrden() {
        return numeroDeOrden;
    }

    public boolean consentimiento() {
        return consentimiento;
    }

    public boolean publicado() {
        return publicado;
    }
}
