package com.ram.vidriera.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "imagenes_banners")
class ImagenBannerJpaEntity {

    @Id
    @Column(name = "banner_id")
    private UUID bannerId;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false)
    private byte[] contenido;

    protected ImagenBannerJpaEntity() {
        // requerido por JPA
    }

    ImagenBannerJpaEntity(UUID bannerId) {
        this.bannerId = bannerId;
    }

    void reemplazar(byte[] contenido) {
        this.contenido = contenido;
    }

    byte[] getContenido() {
        return contenido;
    }
}
