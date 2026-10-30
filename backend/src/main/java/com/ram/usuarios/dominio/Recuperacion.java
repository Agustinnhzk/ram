package com.ram.usuarios.dominio;

import com.ram.usuarios.UsuarioId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Un pedido de "olvidé mi contraseña": vale una sola vez y por poco tiempo. Guarda el HASH del token, nunca el token.
 */
public final class Recuperacion {

    private final UUID id;
    private final UsuarioId cuenta;
    private final String tokenHash;
    private final Instant creadaEn;
    private final Instant venceEn;
    private Instant usadaEn;

    private Recuperacion(UUID id, UsuarioId cuenta, String tokenHash, Instant creadaEn, Instant venceEn, Instant usadaEn) {
        this.id = Objects.requireNonNull(id);
        this.cuenta = Objects.requireNonNull(cuenta);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.creadaEn = Objects.requireNonNull(creadaEn);
        this.venceEn = Objects.requireNonNull(venceEn);
        this.usadaEn = usadaEn;
    }

    public static Recuperacion nueva(UsuarioId cuenta, String tokenHash, Instant ahora, Duration vigencia) {
        return new Recuperacion(UUID.randomUUID(), cuenta, tokenHash, ahora, ahora.plus(vigencia), null);
    }

    public static Recuperacion reconstruir(UUID id, UsuarioId cuenta, String tokenHash, Instant creadaEn, Instant venceEn,
                                           Instant usadaEn) {
        return new Recuperacion(id, cuenta, tokenHash, creadaEn, venceEn, usadaEn);
    }

    public boolean vigente(Instant ahora) {
        return usadaEn == null && ahora.isBefore(venceEn);
    }

    /** Ya no sirve más: se usó, o se pidió un link nuevo. */
    public void anular(Instant ahora) {
        if (usadaEn == null) {
            usadaEn = ahora;
        }
    }

    public UUID id() {
        return id;
    }

    public UsuarioId cuenta() {
        return cuenta;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant creadaEn() {
        return creadaEn;
    }

    public Instant venceEn() {
        return venceEn;
    }

    public Instant usadaEn() {
        return usadaEn;
    }
}
