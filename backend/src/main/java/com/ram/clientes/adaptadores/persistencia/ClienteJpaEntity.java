package com.ram.clientes.adaptadores.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "clientes")
class ClienteJpaEntity {

    @Id
    private UUID id;

    /**
     * El taller dueño de este cliente. Con {@code @TenantId}, Hibernate:
     * <ul>
     *   <li>lo completa solo al guardar, con el taller de la request;</li>
     *   <li>agrega "where taller_id = taller actual" a toda consulta.</li>
     * </ul>
     * Por eso no hay setter ni se carga a mano: no hay forma de poner el taller equivocado.
     */
    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "nombre_busqueda", nullable = false, length = 150)
    private String nombreBusqueda;

    @Column(nullable = false, length = 25)
    private String telefono;

    @Column(length = 254)
    private String correo;

    protected ClienteJpaEntity() {
        // requerido por JPA
    }

    ClienteJpaEntity(UUID id) {
        this.id = id;
    }

    void actualizar(String nombreCompleto, String nombreBusqueda, String telefono, String correo) {
        this.nombreCompleto = nombreCompleto;
        this.nombreBusqueda = nombreBusqueda;
        this.telefono = telefono;
        this.correo = correo;
    }

    UUID getId() {
        return id;
    }

    String getNombreCompleto() {
        return nombreCompleto;
    }

    String getTelefono() {
        return telefono;
    }

    String getCorreo() {
        return correo;
    }
}
