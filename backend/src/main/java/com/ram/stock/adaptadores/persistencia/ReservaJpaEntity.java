package com.ram.stock.adaptadores.persistencia;

import com.ram.stock.EstadoDeReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservas_stock")
class ReservaJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "producto_id", nullable = false, updatable = false)
    private UUID productoId;

    @Column(name = "pedido_id", nullable = false, updatable = false)
    private UUID pedidoId;

    @Column(name = "pedido_numero", nullable = false, updatable = false)
    private int pedidoNumero;

    @Column(nullable = false, updatable = false)
    private int cantidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoDeReserva estado;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn;

    @Column(name = "resuelta_en")
    private Instant resueltaEn;

    protected ReservaJpaEntity() {
        // requerido por JPA
    }

    ReservaJpaEntity(UUID id, UUID productoId, UUID pedidoId, int pedidoNumero, int cantidad, Instant creadaEn) {
        this.id = id;
        this.productoId = productoId;
        this.pedidoId = pedidoId;
        this.pedidoNumero = pedidoNumero;
        this.cantidad = cantidad;
        this.creadaEn = creadaEn;
    }

    void actualizar(EstadoDeReserva estado, Instant resueltaEn) {
        this.estado = estado;
        this.resueltaEn = resueltaEn;
    }

    UUID id() {
        return id;
    }

    UUID productoId() {
        return productoId;
    }

    UUID pedidoId() {
        return pedidoId;
    }

    int pedidoNumero() {
        return pedidoNumero;
    }

    int cantidad() {
        return cantidad;
    }

    EstadoDeReserva estado() {
        return estado;
    }

    Instant creadaEn() {
        return creadaEn;
    }

    Instant resueltaEn() {
        return resueltaEn;
    }
}
