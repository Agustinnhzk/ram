package com.ram.reparaciones.adaptadores.persistencia;

import com.ram.reparaciones.EstadoReparacion;
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

/** @Immutable, como el historial de precios: la bitácora no se corrige, se agrega. */
@Entity
@Immutable
@Table(name = "movimientos_reparacion")
class MovimientoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(nullable = false)
    private int posicion;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoReparacion estado;

    @Column(length = 1000)
    private String nota;

    @Column(name = "visible_para_cliente", nullable = false)
    private boolean visibleParaCliente;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "usuario_nombre", nullable = false, length = 120)
    private String usuarioNombre;

    @Column(name = "ocurrido_en", nullable = false)
    private Instant ocurridoEn;

    protected MovimientoJpaEntity() {
        // requerido por JPA
    }

    MovimientoJpaEntity(UUID id, UUID ordenId, int posicion, EstadoReparacion estado, String nota, boolean visibleParaCliente,
                        UUID usuarioId, String usuarioNombre, Instant ocurridoEn) {
        this.id = id;
        this.ordenId = ordenId;
        this.posicion = posicion;
        this.estado = estado;
        this.nota = nota;
        this.visibleParaCliente = visibleParaCliente;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.ocurridoEn = ocurridoEn;
    }

    UUID getId() {
        return id;
    }

    UUID getOrdenId() {
        return ordenId;
    }

    EstadoReparacion getEstado() {
        return estado;
    }

    String getNota() {
        return nota;
    }

    boolean isVisibleParaCliente() {
        return visibleParaCliente;
    }

    UUID getUsuarioId() {
        return usuarioId;
    }

    String getUsuarioNombre() {
        return usuarioNombre;
    }

    Instant getOcurridoEn() {
        return ocurridoEn;
    }
}
