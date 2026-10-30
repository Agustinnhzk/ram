package com.ram.vidriera.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "banners")
class BannerJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "texto_alternativo", nullable = false, length = 200)
    private String textoAlternativo;

    @Column(nullable = false, length = 300)
    private String enlace;

    @Column(name = "imagen_heredada", length = 300)
    private String imagenHeredada;

    @Column(name = "imagen_version", nullable = false)
    private long imagenVersion;

    @Column(nullable = false)
    private boolean activo;

    private LocalDate desde;

    private LocalDate hasta;

    @Column(nullable = false)
    private int orden;

    protected BannerJpaEntity() {
        // requerido por JPA
    }

    BannerJpaEntity(UUID id) {
        this.id = id;
    }

    void actualizar(String textoAlternativo, String enlace, String imagenHeredada, long imagenVersion, boolean activo,
                    LocalDate desde, LocalDate hasta, int orden) {
        this.textoAlternativo = textoAlternativo;
        this.enlace = enlace;
        this.imagenHeredada = imagenHeredada;
        this.imagenVersion = imagenVersion;
        this.activo = activo;
        this.desde = desde;
        this.hasta = hasta;
        this.orden = orden;
    }

    UUID getId() {
        return id;
    }

    String getTextoAlternativo() {
        return textoAlternativo;
    }

    String getEnlace() {
        return enlace;
    }

    String getImagenHeredada() {
        return imagenHeredada;
    }

    long getImagenVersion() {
        return imagenVersion;
    }

    boolean isActivo() {
        return activo;
    }

    LocalDate getDesde() {
        return desde;
    }

    LocalDate getHasta() {
        return hasta;
    }

    int getOrden() {
        return orden;
    }
}
