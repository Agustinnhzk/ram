package com.ram.pedidos.adaptadores.persistencia;

import com.ram.archivos.TipoDeArchivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

/** Los datos de un comprobante. El archivo está en {@link ArchivoComprobanteJpaEntity}. */
@Entity
@Table(name = "comprobantes_pago")
class ComprobanteJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "nombre_archivo", nullable = false, length = 120, updatable = false)
    private String nombreArchivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TipoDeArchivo tipo;

    @Column(nullable = false, updatable = false)
    private int tamano;

    @Column(name = "subido_en", nullable = false, updatable = false)
    private Instant subidoEn;

    @Column(nullable = false)
    private boolean rechazado;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivoRechazo;

    protected ComprobanteJpaEntity() {
        // requerido por JPA
    }

    ComprobanteJpaEntity(UUID id, String nombreArchivo, TipoDeArchivo tipo, int tamano, Instant subidoEn) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.tipo = tipo;
        this.tamano = tamano;
        this.subidoEn = subidoEn;
    }

    void actualizar(boolean rechazado, String motivoRechazo) {
        this.rechazado = rechazado;
        this.motivoRechazo = motivoRechazo;
    }

    UUID id() {
        return id;
    }

    String nombreArchivo() {
        return nombreArchivo;
    }

    TipoDeArchivo tipo() {
        return tipo;
    }

    int tamano() {
        return tamano;
    }

    Instant subidoEn() {
        return subidoEn;
    }

    boolean rechazado() {
        return rechazado;
    }

    String motivoRechazo() {
        return motivoRechazo;
    }
}
