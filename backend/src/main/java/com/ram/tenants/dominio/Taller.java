package com.ram.tenants.dominio;

import com.ram.tenants.TallerId;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Un taller que usa la plataforma (un tenant).
 *
 * <p>Clase de dominio pura: no conoce Spring, JPA ni HTTP. Las reglas viven en sus
 * métodos (suspender, agregar un dominio) y no en servicios externos, así no hay
 * forma de dejar un Taller en un estado inválido desde afuera.
 */
public class Taller {

    private static final int LARGO_MAXIMO_NOMBRE = 120;

    private final TallerId id;
    private final Slug slug;
    private String nombre;
    private EstadoTaller estado;
    private final Set<Dominio> dominiosPropios;

    private Taller(TallerId id, String nombre, Slug slug, EstadoTaller estado, Set<Dominio> dominiosPropios) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.slug = Objects.requireNonNull(slug, "El slug es obligatorio");
        this.nombre = validarNombre(nombre);
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.dominiosPropios = new LinkedHashSet<>(dominiosPropios);
    }

    /** Alta de un taller nuevo: siempre empieza activo y sin dominio propio. */
    public static Taller crear(String nombre, Slug slug) {
        return new Taller(TallerId.nuevo(), nombre, slug, EstadoTaller.ACTIVO, Set.of());
    }

    /**
     * Rearma un taller que ya existía (por ejemplo, leído de la base de datos).
     * No es un alta: no aplica las reglas de creación.
     */
    public static Taller reconstruir(TallerId id, String nombre, Slug slug,
                                     EstadoTaller estado, Set<Dominio> dominiosPropios) {
        return new Taller(id, nombre, slug, estado, dominiosPropios);
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = validarNombre(nuevoNombre);
    }

    public void agregarDominioPropio(Dominio dominio) {
        dominiosPropios.add(Objects.requireNonNull(dominio, "El dominio es obligatorio"));
    }

    public void quitarDominioPropio(Dominio dominio) {
        dominiosPropios.remove(dominio);
    }

    public void suspender() {
        this.estado = EstadoTaller.SUSPENDIDO;
    }

    public void reactivar() {
        this.estado = EstadoTaller.ACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoTaller.ACTIVO;
    }

    public TallerId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public Slug slug() {
        return slug;
    }

    public EstadoTaller estado() {
        return estado;
    }

    /** Copia de solo lectura: para cambiarlos hay que pasar por los métodos del taller. */
    public Set<Dominio> dominiosPropios() {
        return Collections.unmodifiableSet(dominiosPropios);
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del taller es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.length() > LARGO_MAXIMO_NOMBRE) {
            throw new IllegalArgumentException(
                    "El nombre del taller no puede superar " + LARGO_MAXIMO_NOMBRE + " caracteres");
        }
        return limpio;
    }

    /** Dos talleres son el mismo si tienen el mismo id, aunque cambie su nombre. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Taller otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
