package com.ram.instalaciones.adaptadores.persistencia;

import com.ram.instalaciones.EstadoInstalacion;
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
@Table(name = "instalaciones")
class InstalacionJpaEntity {

    @Id
    UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    UUID tallerId;

    @Column(nullable = false, updatable = false)
    int numero;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    UUID clienteId;

    @Column(nullable = false, length = 300)
    String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    EstadoInstalacion estado;

    @Column(length = 1000)
    String equipos;

    Integer camaras;

    LocalDate fecha;

    @Column(length = 120)
    String tecnico;

    Long presupuesto;

    @Column(length = 2000)
    String notas;

    @Column(name = "creada_en", nullable = false, updatable = false)
    Instant creadaEn;

    protected InstalacionJpaEntity() {
        // requerido por JPA
    }

    InstalacionJpaEntity(UUID id) {
        this.id = id;
    }
}
