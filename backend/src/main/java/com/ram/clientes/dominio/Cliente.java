package com.ram.clientes.dominio;

import com.ram.clientes.ClienteId;

import java.util.Objects;
import java.util.Optional;

/**
 * Un cliente del taller.
 *
 * <p>Fijate que no tiene un campo "taller": el dominio no necesita saberlo.
 * A qué taller pertenece lo resuelve la persistencia (ADR-005), así ninguna regla
 * del negocio puede equivocarse de taller.
 */
public class Cliente {

    private final ClienteId id;
    private NombreCompleto nombre;
    private Telefono telefono;
    private Correo correo;

    private Cliente(ClienteId id, NombreCompleto nombre, Telefono telefono, Correo correo) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio");
        this.telefono = Objects.requireNonNull(telefono, "El teléfono es obligatorio");
        this.correo = correo;
    }

    public static Cliente registrar(NombreCompleto nombre, Telefono telefono, Optional<Correo> correo) {
        return new Cliente(ClienteId.nuevo(), nombre, telefono, correo.orElse(null));
    }

    public static Cliente reconstruir(ClienteId id, NombreCompleto nombre, Telefono telefono, Optional<Correo> correo) {
        return new Cliente(id, nombre, telefono, correo.orElse(null));
    }

    public void actualizarContacto(Telefono nuevoTelefono, Optional<Correo> nuevoCorreo) {
        this.telefono = Objects.requireNonNull(nuevoTelefono, "El teléfono es obligatorio");
        this.correo = nuevoCorreo.orElse(null);
    }

    public void corregirNombre(NombreCompleto nuevoNombre) {
        this.nombre = Objects.requireNonNull(nuevoNombre, "El nombre es obligatorio");
    }

    public ClienteId id() {
        return id;
    }

    public NombreCompleto nombre() {
        return nombre;
    }

    public Telefono telefono() {
        return telefono;
    }

    public Optional<Correo> correo() {
        return Optional.ofNullable(correo);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cliente otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
