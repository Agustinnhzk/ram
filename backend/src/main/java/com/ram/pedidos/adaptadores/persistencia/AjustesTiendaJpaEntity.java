package com.ram.pedidos.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "ajustes_tienda")
class AjustesTiendaJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "precio_armado_pc")
    private Long precioArmadoPc;

    protected AjustesTiendaJpaEntity() {
        // requerido por JPA
    }

    AjustesTiendaJpaEntity(UUID id) {
        this.id = id;
    }

    void cambiarPrecioArmadoPc(Long precio) {
        this.precioArmadoPc = precio;
    }

    Long precioArmadoPc() {
        return precioArmadoPc;
    }
}
