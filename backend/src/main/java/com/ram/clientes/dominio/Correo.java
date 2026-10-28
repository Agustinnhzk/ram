package com.ram.clientes.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Correo del cliente. Es opcional en el cliente; si existe, es válido.
 *
 * <p>La validación es simple a propósito (algo@algo.algo). La única prueba real
 * de que un correo existe es que le llegue el comprobante.
 */
public record Correo(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Correo {
        Objects.requireNonNull(valor, "El correo no puede ser nulo");
        valor = valor.trim().toLowerCase(Locale.ROOT);
        if (valor.length() > 254 || !FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Correo inválido: '" + valor + "'");
        }
    }

    /** Para datos que pueden venir vacíos del formulario: null o "  " significan "sin correo". */
    public static Optional<Correo> opcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new Correo(valor));
    }
}
