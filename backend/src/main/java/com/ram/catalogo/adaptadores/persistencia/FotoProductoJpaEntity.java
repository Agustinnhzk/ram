package com.ram.catalogo.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

/** Los datos de una foto (sin los bytes, que están en archivos_fotos: listar fotos no carga imágenes). */
@Entity
@Table(name = "fotos_productos")
class FotoProductoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "producto_id", nullable = false, updatable = false)
    private UUID productoId;

    @Column(nullable = false)
    private int posicion;

    @Column(nullable = false, updatable = false)
    private int ancho;

    @Column(nullable = false, updatable = false)
    private int alto;

    @Column(name = "subida_en", nullable = false, updatable = false)
    private Instant subidaEn;

    protected FotoProductoJpaEntity() {
        // requerido por JPA
    }

    FotoProductoJpaEntity(UUID id, UUID productoId, int posicion, int ancho, int alto, Instant subidaEn) {
        this.id = id;
        this.productoId = productoId;
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

    UUID getProductoId() {
        return productoId;
    }

    int getPosicion() {
        return posicion;
    }
}
