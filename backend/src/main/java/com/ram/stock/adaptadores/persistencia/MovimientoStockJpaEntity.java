package com.ram.stock.adaptadores.persistencia;

import com.ram.stock.TipoDeMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

/** El historial: se inserta y nunca se cambia (@Immutable). */
@Entity
@Immutable
@Table(name = "movimientos_stock")
class MovimientoStockJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDeMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int saldo;

    @Column(length = 300)
    private String nota;

    @Column(name = "pedido_id")
    private UUID pedidoId;

    @Column(name = "pedido_numero")
    private Integer pedidoNumero;

    @Column(length = 120)
    private String usuario;

    @Column(nullable = false)
    private Instant fecha;

    protected MovimientoStockJpaEntity() {
        // requerido por JPA
    }

    MovimientoStockJpaEntity(UUID id, UUID productoId, TipoDeMovimiento tipo, int cantidad, int saldo, String nota,
                             UUID pedidoId, Integer pedidoNumero, String usuario, Instant fecha) {
        this.id = id;
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.saldo = saldo;
        this.nota = nota;
        this.pedidoId = pedidoId;
        this.pedidoNumero = pedidoNumero;
        this.usuario = usuario;
        this.fecha = fecha;
    }

    UUID id() {
        return id;
    }

    UUID productoId() {
        return productoId;
    }

    TipoDeMovimiento tipo() {
        return tipo;
    }

    int cantidad() {
        return cantidad;
    }

    int saldo() {
        return saldo;
    }

    String nota() {
        return nota;
    }

    UUID pedidoId() {
        return pedidoId;
    }

    Integer pedidoNumero() {
        return pedidoNumero;
    }

    String usuario() {
        return usuario;
    }

    Instant fecha() {
        return fecha;
    }
}
