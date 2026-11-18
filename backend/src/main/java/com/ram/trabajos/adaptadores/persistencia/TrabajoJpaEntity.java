package com.ram.trabajos.adaptadores.persistencia;

import com.ram.trabajos.TipoDeTrabajo;
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
@Table(name = "trabajos")
class TrabajoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, updatable = false, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoDeTrabajo tipo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 60)
    private String barrio;

    @Column(name = "barrio_clave", nullable = false, length = 60)
    private String barrioClave;

    @Column(name = "a_domicilio", nullable = false)
    private boolean aDomicilio;

    @Column(nullable = false, length = 1000)
    private String problema;

    @Column(length = 1000)
    private String diagnostico;

    @Column(nullable = false, length = 1000)
    private String solucion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "numero_de_orden")
    private Integer numeroDeOrden;

    @Column(nullable = false)
    private boolean consentimiento;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected TrabajoJpaEntity() {
        // requerido por JPA
    }

    TrabajoJpaEntity(UUID id, String slug, Instant creadoEn) {
        this.id = id;
        this.slug = slug;
        this.creadoEn = creadoEn;
    }

    void actualizar(TipoDeTrabajo tipo, String titulo, String barrio, String barrioClave, boolean aDomicilio, String problema,
                    String diagnostico, String solucion, LocalDate fecha, Integer numeroDeOrden, boolean consentimiento,
                    boolean publicado) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.barrio = barrio;
        this.barrioClave = barrioClave;
        this.aDomicilio = aDomicilio;
        this.problema = problema;
        this.diagnostico = diagnostico;
        this.solucion = solucion;
        this.fecha = fecha;
        this.numeroDeOrden = numeroDeOrden;
        this.consentimiento = consentimiento;
        this.publicado = publicado;
    }

    UUID getId() {
        return id;
    }

    String getSlug() {
        return slug;
    }

    TipoDeTrabajo getTipo() {
        return tipo;
    }

    String getTitulo() {
        return titulo;
    }

    String getBarrio() {
        return barrio;
    }

    String getBarrioClave() {
        return barrioClave;
    }

    boolean isADomicilio() {
        return aDomicilio;
    }

    String getProblema() {
        return problema;
    }

    String getDiagnostico() {
        return diagnostico;
    }

    String getSolucion() {
        return solucion;
    }

    LocalDate getFecha() {
        return fecha;
    }

    Integer getNumeroDeOrden() {
        return numeroDeOrden;
    }

    boolean isConsentimiento() {
        return consentimiento;
    }

    boolean isPublicado() {
        return publicado;
    }

    Instant getCreadoEn() {
        return creadoEn;
    }
}
