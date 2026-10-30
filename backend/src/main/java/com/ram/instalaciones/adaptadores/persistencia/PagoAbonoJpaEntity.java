package com.ram.instalaciones.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** Una fila = ese abonado pagó ese mes. "No pagó" = no hay fila. */
@Entity
@Table(name = "pagos_abono")
class PagoAbonoJpaEntity {

    @Id
    UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    UUID tallerId;

    @Column(name = "abonado_id", nullable = false, updatable = false)
    UUID abonadoId;

    @Column(nullable = false, updatable = false)
    int anio;

    @Column(nullable = false, updatable = false)
    int mes;

    protected PagoAbonoJpaEntity() {
        // requerido por JPA
    }

    PagoAbonoJpaEntity(UUID abonadoId, int anio, int mes) {
        this.id = UUID.randomUUID();
        this.abonadoId = abonadoId;
        this.anio = anio;
        this.mes = mes;
    }
}
