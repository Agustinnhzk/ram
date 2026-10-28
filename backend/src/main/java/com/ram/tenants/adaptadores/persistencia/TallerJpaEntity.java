package com.ram.tenants.adaptadores.persistencia;

import com.ram.tenants.dominio.EstadoTaller;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Cómo se guarda un taller en la base. Es una clase distinta de {@link com.ram.tenants.dominio.Taller}
 * a propósito: lo que pide JPA (constructor vacío, setters, anotaciones) no ensucia el dominio,
 * y la tabla puede cambiar sin tocar las reglas del negocio.
 */
@Entity
@Table(name = "talleres")
class TallerJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 63)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTaller estado;

    /** Un taller tiene pocos dominios y casi siempre se necesitan: se cargan junto con él. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "talleres_dominios", joinColumns = @JoinColumn(name = "taller_id"))
    @Column(name = "dominio", nullable = false, length = 253)
    private Set<String> dominios = new HashSet<>();

    protected TallerJpaEntity() {
        // requerido por JPA
    }

    TallerJpaEntity(UUID id, String nombre, String slug, EstadoTaller estado, Set<String> dominios) {
        this.id = id;
        this.nombre = nombre;
        this.slug = slug;
        this.estado = estado;
        this.dominios = new HashSet<>(dominios);
    }

    UUID getId() {
        return id;
    }

    String getNombre() {
        return nombre;
    }

    String getSlug() {
        return slug;
    }

    EstadoTaller getEstado() {
        return estado;
    }

    Set<String> getDominios() {
        return dominios;
    }
}
