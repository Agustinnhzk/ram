package com.ram.pedidos.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** El archivo de un comprobante: byte[] ↔ BYTEA. Solo se carga al descargarlo. */
@Entity
@Immutable
@Table(name = "archivos_comprobantes")
class ArchivoComprobanteJpaEntity {

    @Id
    @Column(name = "comprobante_id")
    private UUID comprobanteId;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false)
    private byte[] contenido;

    protected ArchivoComprobanteJpaEntity() {
        // requerido por JPA
    }

    ArchivoComprobanteJpaEntity(UUID comprobanteId, byte[] contenido) {
        this.comprobanteId = comprobanteId;
        this.contenido = contenido;
    }

    byte[] contenido() {
        return contenido;
    }
}
