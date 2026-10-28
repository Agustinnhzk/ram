package com.ram.usuarios.adaptadores.persistencia;

import com.ram.usuarios.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "usuarios_personal")
class PersonalJpaEntity {

    @Id
    private UUID id;

    /** Igual que en clientes: Hibernate completa y filtra el taller solo (ADR-005). */
    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 254)
    private String correo;

    @Column(name = "clave_cifrada", nullable = false, length = 100)
    private String claveCifrada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    protected PersonalJpaEntity() {
        // requerido por JPA
    }

    PersonalJpaEntity(UUID id) {
        this.id = id;
    }

    void actualizar(String nombre, String correo, String claveCifrada, Rol rol, boolean activo) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveCifrada = claveCifrada;
        this.rol = rol;
        this.activo = activo;
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

    Rol getRol() {
        return rol;
    }

    boolean isActivo() {
        return activo;
    }
}
