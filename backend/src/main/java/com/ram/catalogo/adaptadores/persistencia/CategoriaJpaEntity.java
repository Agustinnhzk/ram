package com.ram.catalogo.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** Las categorías de cada taller. Se crean y editan desde el sistema (ADR-013); el slug no cambia nunca. */
@Entity
@Table(name = "categorias")
class CategoriaJpaEntity {

    @Id
    private UUID id;

    /** Ver ClienteJpaEntity: Hibernate lo completa y filtra solo, con el taller de la request. */
    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, length = 60)
    private String slug;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "con_instalacion", nullable = false)
    private boolean conInstalacion;

    @Column(nullable = false)
    private int orden;

    protected CategoriaJpaEntity() {
        // requerido por JPA
    }

    CategoriaJpaEntity(UUID id, String slug, int orden) {
        this.id = id;
        this.slug = slug;
        this.orden = orden;
    }

    void actualizar(String nombre, boolean conInstalacion) {
        this.nombre = nombre;
        this.conInstalacion = conInstalacion;
    }

    UUID getId() {
        return id;
    }

    String getSlug() {
        return slug;
    }

    String getNombre() {
        return nombre;
    }

    boolean isConInstalacion() {
        return conInstalacion;
    }
}
