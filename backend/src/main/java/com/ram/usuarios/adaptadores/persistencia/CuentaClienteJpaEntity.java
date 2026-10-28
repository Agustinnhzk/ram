package com.ram.usuarios.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "cuentas_clientes")
class CuentaClienteJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 254)
    private String correo;

    @Column(name = "clave_cifrada", nullable = false, length = 100)
    private String claveCifrada;

    @Column(name = "correo_verificado", nullable = false)
    private boolean correoVerificado;

    protected CuentaClienteJpaEntity() {
        // requerido por JPA
    }

    CuentaClienteJpaEntity(UUID id) {
        this.id = id;
    }

    void actualizar(String nombre, String correo, String claveCifrada, boolean correoVerificado) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveCifrada = claveCifrada;
        this.correoVerificado = correoVerificado;
    }

    UUID getId() {
        return id;
    }

    String getNombre() {
        return nombre;
    }

    String getCorreo() {
        return correo;
    }

    String getClaveCifrada() {
        return claveCifrada;
    }

    boolean isCorreoVerificado() {
        return correoVerificado;
    }
}
