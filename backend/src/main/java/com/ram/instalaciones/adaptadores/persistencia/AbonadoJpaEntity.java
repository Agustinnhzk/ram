package com.ram.instalaciones.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "abonados_monitoreo")
class AbonadoJpaEntity {

    @Id
    UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    UUID tallerId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    UUID clienteId;

    @Column(length = 300)
    String direccion;

    Long monto;

    @Column(nullable = false)
    boolean activo;

    protected AbonadoJpaEntity() {
        // requerido por JPA
    }

    AbonadoJpaEntity(UUID id) {
        this.id = id;
    }
}
