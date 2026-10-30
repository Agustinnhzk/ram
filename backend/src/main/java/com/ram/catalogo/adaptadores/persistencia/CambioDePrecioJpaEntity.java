package com.ram.catalogo.adaptadores.persistencia;

import com.ram.catalogo.MotivoDeCambio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** @Immutable: Hibernate nunca manda un UPDATE de esta tabla. El historial no se corrige: se agrega. */
@Entity
@Immutable
@Table(name = "historial_precios")
class CambioDePrecioJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "precio_anterior")
    private Long precioAnterior;

    @Column(name = "precio_nuevo")
    private Long precioNuevo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoDeCambio motivo;

    @Column(precision = 6, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "usuario_nombre", nullable = false, length = 120)
    private String usuarioNombre;

    @Column(name = "cambiado_en", nullable = false)
    private Instant cambiadoEn;

    protected CambioDePrecioJpaEntity() {
        // requerido por JPA
    }

    CambioDePrecioJpaEntity(UUID productoId, Long precioAnterior, Long precioNuevo, MotivoDeCambio motivo,
                            BigDecimal porcentaje, UUID usuarioId, String usuarioNombre, Instant cambiadoEn) {
        this.id = UUID.randomUUID();
        this.productoId = productoId;
        this.precioAnterior = precioAnterior;
        this.precioNuevo = precioNuevo;
        this.motivo = motivo;
        this.porcentaje = porcentaje;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.cambiadoEn = cambiadoEn;
    }

    UUID getProductoId() {
        return productoId;
    }

    Long getPrecioAnterior() {
        return precioAnterior;
    }

    Long getPrecioNuevo() {
        return precioNuevo;
    }

    MotivoDeCambio getMotivo() {
        return motivo;
    }

    BigDecimal getPorcentaje() {
        return porcentaje;
    }

    UUID getUsuarioId() {
        return usuarioId;
    }

    String getUsuarioNombre() {
        return usuarioNombre;
    }

    Instant getCambiadoEn() {
        return cambiadoEn;
    }
}
