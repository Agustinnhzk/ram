package com.ram.pedidos.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "datos_de_pago")
class DatosDePagoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, length = 60)
    private String alias;

    @Column(nullable = false, length = 120)
    private String titular;

    @Column(nullable = false, length = 60)
    private String entidad;

    @Column(length = 22)
    private String cbu;

    protected DatosDePagoJpaEntity() {
        // requerido por JPA
    }

    String alias() {
        return alias;
    }

    String titular() {
        return titular;
    }

    String entidad() {
        return entidad;
    }

    String cbu() {
        return cbu;
    }
}
