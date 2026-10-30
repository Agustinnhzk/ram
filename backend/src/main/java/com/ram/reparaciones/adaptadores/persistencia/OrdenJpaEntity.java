package com.ram.reparaciones.adaptadores.persistencia;

import com.ram.reparaciones.EstadoReparacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ordenes_reparacion")
class OrdenJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, updatable = false)
    private int numero;

    /** Solo el id: el cliente es de otro módulo y se consulta por su API, no con un join. */
    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(nullable = false, length = 150)
    private String equipo;

    @Column(name = "con_cargador", nullable = false)
    private boolean conCargador;

    @Column(length = 100)
    private String procesador;

    @Column(name = "password_equipo", length = 100)
    private String passwordEquipo;

    @Column(nullable = false, length = 1000)
    private String problema;

    @Column(length = 1000)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReparacion estado;

    @Column(name = "presupuesto_monto")
    private Long presupuestoMonto;

    @Column(name = "presupuesto_descripcion", length = 1000)
    private String presupuestoDescripcion;

    @Column(name = "ingresado_en", nullable = false, updatable = false)
    private Instant ingresadoEn;

    @Column(name = "entregado_en")
    private Instant entregadoEn;

    @Column(name = "garantia_dias")
    private Integer garantiaDias;

    @Column(name = "garantia_hasta")
    private LocalDate garantiaHasta;

    protected OrdenJpaEntity() {
        // requerido por JPA
    }

    OrdenJpaEntity(UUID id, int numero, UUID clienteId, Instant ingresadoEn) {
        this.id = id;
        this.numero = numero;
        this.clienteId = clienteId;
        this.ingresadoEn = ingresadoEn;
    }

    void actualizar(String equipo, boolean conCargador, String procesador, String passwordEquipo, String problema,
                    String observaciones, EstadoReparacion estado, Long presupuestoMonto, String presupuestoDescripcion,
                    Instant entregadoEn, Integer garantiaDias, LocalDate garantiaHasta) {
        this.equipo = equipo;
        this.conCargador = conCargador;
        this.procesador = procesador;
        this.passwordEquipo = passwordEquipo;
        this.problema = problema;
        this.observaciones = observaciones;
        this.estado = estado;
        this.presupuestoMonto = presupuestoMonto;
        this.presupuestoDescripcion = presupuestoDescripcion;
        this.entregadoEn = entregadoEn;
        this.garantiaDias = garantiaDias;
        this.garantiaHasta = garantiaHasta;
    }

    UUID getId() {
        return id;
    }

    int getNumero() {
        return numero;
    }

    UUID getClienteId() {
        return clienteId;
    }

    String getEquipo() {
        return equipo;
    }

    boolean isConCargador() {
        return conCargador;
    }

    String getProcesador() {
        return procesador;
    }

    String getPasswordEquipo() {
        return passwordEquipo;
    }

    String getProblema() {
        return problema;
    }

    String getObservaciones() {
        return observaciones;
    }

    EstadoReparacion getEstado() {
        return estado;
    }

    Long getPresupuestoMonto() {
        return presupuestoMonto;
    }

    String getPresupuestoDescripcion() {
        return presupuestoDescripcion;
    }

    Instant getIngresadoEn() {
        return ingresadoEn;
    }

    Instant getEntregadoEn() {
        return entregadoEn;
    }

    Integer getGarantiaDias() {
        return garantiaDias;
    }

    LocalDate getGarantiaHasta() {
        return garantiaHasta;
    }

    /** Sin el password: si Hibernate o un log imprime la entidad, no se filtra (ADR-009). */
    @Override
    public String toString() {
        return "OrdenJpaEntity[numero=" + numero + ", estado=" + estado + "]";
    }
}
