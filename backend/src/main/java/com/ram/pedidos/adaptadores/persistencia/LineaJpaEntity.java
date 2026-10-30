package com.ram.pedidos.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/** Una línea del pedido. @Immutable: una vez guardada, Hibernate nunca la actualiza. */
@Entity
@Immutable
@Table(name = "lineas_pedido")
class LineaJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false)
    private int posicion;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false)
    private String[] detalle;

    @Column(nullable = false, length = 400)
    private String referencia;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private long precioUnitario;

    /** Los productos de UNA unidad de la línea (ADR-014). Vacío en pedidos viejos y en el servicio de armado. */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false)
    private UUID[] productos;

    @Column(name = "sin_stock", nullable = false)
    private boolean sinStock;

    protected LineaJpaEntity() {
        // requerido por JPA
    }

    LineaJpaEntity(int posicion, String descripcion, String[] detalle, String referencia, int cantidad, long precioUnitario,
                   UUID[] productos, boolean sinStock) {
        this.id = UUID.randomUUID();
        this.posicion = posicion;
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.referencia = referencia;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.productos = productos;
        this.sinStock = sinStock;
    }

    String descripcion() {
        return descripcion;
    }

    String[] detalle() {
        return detalle;
    }

    String referencia() {
        return referencia;
    }

    int cantidad() {
        return cantidad;
    }

    long precioUnitario() {
        return precioUnitario;
    }

    UUID[] productos() {
        return productos == null ? new UUID[0] : productos;
    }

    boolean sinStock() {
        return sinStock;
    }
}
