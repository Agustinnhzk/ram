package com.ram.usuarios.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recuperaciones_de_clave")
class RecuperacionJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "cuenta_id", nullable = false, updatable = false)
    private UUID cuentaId;

    @Column(name = "token_hash", nullable = false, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn;

    @Column(name = "vence_en", nullable = false, updatable = false)
    private Instant venceEn;

    @Column(name = "usada_en")
    private Instant usadaEn;

    protected RecuperacionJpaEntity() {
        // requerido por JPA
    }

    RecuperacionJpaEntity(UUID id, UUID cuentaId, String tokenHash, Instant creadaEn, Instant venceEn) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.tokenHash = tokenHash;
        this.creadaEn = creadaEn;
        this.venceEn = venceEn;
    }

    void marcarUsada(Instant usadaEn) {
        this.usadaEn = usadaEn;
    }

    UUID getId() {
        return id;
    }

    UUID getCuentaId() {
        return cuentaId;
    }

    String getTokenHash() {
        return tokenHash;
    }

    Instant getCreadaEn() {
        return creadaEn;
    }

    Instant getVenceEn() {
        return venceEn;
    }

    Instant getUsadaEn() {
        return usadaEn;
    }
}
