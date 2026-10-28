package com.ram.tenants.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Nombre corto del taller que forma su subdominio: {@code ram} en {@code ram.plataforma.com.ar}.
 *
 * <p>Value object: se valida al crearse, así que si existe un Slug, es válido.
 * El resto del código no necesita volver a chequearlo.
 */
public record Slug(String valor) {

    /** Mismas reglas que una etiqueta DNS: minúsculas, números y guiones, hasta 63 caracteres. */
    private static final Pattern FORMATO = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$");

    /**
     * Palabras que la plataforma usa para otras cosas. "sistema" es el prefijo del
     * sistema de gestión (sistema.ram.plataforma.com.ar), así que ningún taller puede llamarse así.
     */
    private static final Set<String> RESERVADOS = Set.of("www", "api", "sistema", "admin");

    public Slug {
        Objects.requireNonNull(valor, "El slug no puede ser nulo");
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Slug inválido: '" + valor + "'");
        }
        if (RESERVADOS.contains(valor)) {
            throw new IllegalArgumentException("El slug '" + valor + "' está reservado");
        }
    }

    /** Crea un slug normalizando mayúsculas y espacios: " RAM " pasa a "ram". */
    public static Slug de(String valor) {
        Objects.requireNonNull(valor, "El slug no puede ser nulo");
        return new Slug(valor.trim().toLowerCase(Locale.ROOT));
    }
}
