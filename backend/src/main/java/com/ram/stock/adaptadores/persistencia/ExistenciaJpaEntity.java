package com.ram.stock.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

/** Una fila por producto con control de stock. Sin fila = sin control. */
@Entity
@Table(name = "existencias")
class ExistenciaJpaEntity {

    @Id
    @Column(name = "producto_id")
    private UUID productoId;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected ExistenciaJpaEntity() {
        // requerido por JPA
    }

    ExistenciaJpaEntity(UUID productoId) {
        this.productoId = productoId;
    }

    void actualizar(int cantidad, Instant ahora) {
        this.cantidad = cantidad;
        this.actualizadoEn = ahora;
    }

    UUID productoId() {
        return productoId;
    }

    int cantidad() {
        return cantidad;
    }
}
