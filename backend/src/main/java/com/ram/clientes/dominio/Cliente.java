package com.ram.clientes.dominio;

import com.ram.clientes.ClienteId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Un cliente del taller: la ficha que se usa en reparaciones y, si compra en la tienda, la que se vincula
 * con su cuenta (ADR-014).
 *
 * <p>Fijate que no tiene un campo "taller": el dominio no necesita saberlo.
 * A qué taller pertenece lo resuelve la persistencia (ADR-005), así ninguna regla
 * del negocio puede equivocarse de taller.
 */
public class Cliente {

    public static final int LARGO_MAXIMO_NOTAS = 2000;

    private final ClienteId id;
    private NombreCompleto nombre;
    private Telefono telefono;
    private Correo correo;
    private String notas;
    private UUID cuentaTienda;
    private final Instant creadoEn;

    private Cliente(ClienteId id, NombreCompleto nombre, Telefono telefono, Correo correo, String notas, UUID cuentaTienda,
                    Instant creadoEn) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio");
        this.telefono = Objects.requireNonNull(telefono, "El teléfono es obligatorio");
        this.correo = correo;
        this.notas = notas;
        this.cuentaTienda = cuentaTienda;
        this.creadoEn = creadoEn;
    }

    public static Cliente registrar(NombreCompleto nombre, Telefono telefono, Optional<Correo> correo) {
        return new Cliente(ClienteId.nuevo(), nombre, telefono, correo.orElse(null), null, null, null);
    }

    public static Cliente reconstruir(ClienteId id, NombreCompleto nombre, Telefono telefono, Optional<Correo> correo) {
        return reconstruir(id, nombre, telefono, correo, null, null, null);
    }

    public static Cliente reconstruir(ClienteId id, NombreCompleto nombre, Telefono telefono, Optional<Correo> correo,
                                      String notas, UUID cuentaTienda, Instant creadoEn) {
        return new Cliente(id, nombre, telefono, correo.orElse(null), notas, cuentaTienda, creadoEn);
    }

    public void actualizarContacto(Telefono nuevoTelefono, Optional<Correo> nuevoCorreo) {
        this.telefono = Objects.requireNonNull(nuevoTelefono, "El teléfono es obligatorio");
        this.correo = nuevoCorreo.orElse(null);
    }

    public void corregirNombre(NombreCompleto nuevoNombre) {
        this.nombre = Objects.requireNonNull(nuevoNombre, "El nombre es obligatorio");
    }

    /** Notas internas del taller (nunca las ve el cliente). Vacías = sin notas. */
    public void cambiarNotas(String texto) {
        if (texto == null || texto.isBlank()) {
            this.notas = null;
            return;
        }
        String limpio = texto.strip();
        if (limpio.length() > LARGO_MAXIMO_NOTAS) {
            throw new IllegalArgumentException("Las notas no pueden pasar de " + LARGO_MAXIMO_NOTAS + " caracteres");
        }
        this.notas = limpio;
    }

    /** Si la ficha no tiene correo, toma el de la cuenta con la que se vincula. */
    public void vincularCuenta(UUID cuenta, Optional<Correo> correoDeLaCuenta) {
        this.cuentaTienda = Objects.requireNonNull(cuenta);
        if (this.correo == null) {
            this.correo = correoDeLaCuenta.orElse(null);
        }
    }

    public void desvincularCuenta() {
        this.cuentaTienda = null;
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

    public Optional<String> notas() {
        return Optional.ofNullable(notas);
    }

    public Optional<UUID> cuentaTienda() {
        return Optional.ofNullable(cuentaTienda);
    }

    /** null en una ficha recién creada que todavía no se leyó de la base (la fecha la pone la base). */
    public Instant creadoEn() {
        return creadoEn;
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
