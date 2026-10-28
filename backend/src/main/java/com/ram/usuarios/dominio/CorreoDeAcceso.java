package com.ram.usuarios.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * El correo con el que alguien inicia sesión. Se guarda en minúsculas:
 * "Agus@Mail.com" y "agus@mail.com" son la misma cuenta.
 *
 * <p>Es parecido al Correo de clientes, pero no se comparte a propósito: cada módulo
 * es dueño de su dominio (ADR-001). Duplicar un value object chico es más barato
 * que acoplar dos módulos.
 */
public record CorreoDeAcceso(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public CorreoDeAcceso {
        Objects.requireNonNull(valor, "El correo es obligatorio");
        valor = valor.trim().toLowerCase(Locale.ROOT);
        if (valor.length() > 254 || !FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Correo inválido");
        }
    }

    /** Para el login: un correo mal escrito no es un error de validación, es "credenciales inválidas". */
    public static Optional<CorreoDeAcceso> intentar(String valor) {
        try {
            return Optional.of(new CorreoDeAcceso(valor));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
