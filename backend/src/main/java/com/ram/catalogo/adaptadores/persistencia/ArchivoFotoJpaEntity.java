package com.ram.catalogo.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** Los dos tamaños de una foto, en JPEG. Solo se cargan al pedir la imagen. */
@Entity
@Immutable
@Table(name = "archivos_fotos")
class ArchivoFotoJpaEntity {

    @Id
    @Column(name = "foto_id")
    private UUID fotoId;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false)
    private byte[] grande;

    @Column(nullable = false)
    private byte[] chica;

    protected ArchivoFotoJpaEntity() {
        // requerido por JPA
    }

    ArchivoFotoJpaEntity(UUID fotoId, byte[] grande, byte[] chica) {
        this.fotoId = fotoId;
        this.grande = grande;
        this.chica = chica;
    }

    byte[] getGrande() {
        return grande;
    }

    byte[] getChica() {
        return chica;
    }
}
