package com.ram.trabajos.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

/** Los datos de una foto (sin los bytes, que están en archivos_fotos_trabajos: listar no carga imágenes). */
@Entity
@Table(name = "fotos_trabajos")
class FotoTrabajoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "trabajo_id", nullable = false, updatable = false)
    private UUID trabajoId;

    @Column(nullable = false)
    private int posicion;

    @Column(nullable = false, updatable = false)
    private int ancho;

    @Column(nullable = false, updatable = false)
    private int alto;

    @Column(name = "subida_en", nullable = false, updatable = false)
    private Instant subidaEn;

    protected FotoTrabajoJpaEntity() {
        // requerido por JPA
    }

    FotoTrabajoJpaEntity(UUID id, UUID trabajoId, int posicion, int ancho, int alto, Instant subidaEn) {
        this.id = id;
        this.trabajoId = trabajoId;
        this.posicion = posicion;
        this.ancho = ancho;
        this.alto = alto;
        this.subidaEn = subidaEn;
    }

    void moverA(int posicion) {
        this.posicion = posicion;
    }

    UUID getId() {
        return id;
    }

    UUID getTrabajoId() {
        return trabajoId;
    }

    int getPosicion() {
        return posicion;
    }
}
