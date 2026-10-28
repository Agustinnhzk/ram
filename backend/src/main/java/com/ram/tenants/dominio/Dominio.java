package com.ram.tenants.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Un nombre de dominio ya normalizado: {@code ramservicios.com.ar}, {@code ram.localhost}.
 */
public record Dominio(String valor) {

    private static final String ETIQUETA = "[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?";
    private static final Pattern FORMATO =
            Pattern.compile("^(?=.{1,253}$)" + ETIQUETA + "(\\." + ETIQUETA + ")*$");

    public Dominio {
        Objects.requireNonNull(valor, "El dominio no puede ser nulo");
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Dominio inválido: '" + valor + "'");
        }
    }

    /**
     * Crea un dominio a partir de lo que llega en la request, normalizándolo:
     * minúsculas, sin espacios, sin puerto y sin el punto final opcional del DNS.
     * "RAM.localhost:8080" pasa a "ram.localhost".
     */
    public static Dominio de(String host) {
        Objects.requireNonNull(host, "El dominio no puede ser nulo");
        String normalizado = host.trim().toLowerCase(Locale.ROOT);
        int puerto = normalizado.indexOf(':');
        if (puerto >= 0) {
            normalizado = normalizado.substring(0, puerto);
        }
        if (normalizado.endsWith(".")) {
            normalizado = normalizado.substring(0, normalizado.length() - 1);
        }
        return new Dominio(normalizado);
    }

    /** Igual que {@link #de(String)}, pero devuelve vacío en vez de fallar si el host no es válido. */
    public static Optional<Dominio> intentar(String host) {
        if (host == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(de(host));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Si este dominio es un subdominio directo de {@code base}, devuelve el slug.
     *
     * <p>Con base "plataforma.com.ar": "ram.plataforma.com.ar" da "ram";
     * "plataforma.com.ar" y "a.b.plataforma.com.ar" dan vacío.
     */
    public Optional<Slug> subdominioDe(Dominio base) {
        String sufijo = "." + base.valor;
        if (!valor.endsWith(sufijo)) {
            return Optional.empty();
        }
        String izquierda = valor.substring(0, valor.length() - sufijo.length());
        if (izquierda.contains(".")) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Slug(izquierda));
        } catch (IllegalArgumentException e) {
            // Por ejemplo "www": es subdominio de la plataforma, pero no de un taller.
            return Optional.empty();
        }
    }

    /**
     * Quita un prefijo si lo tiene: "sistema.ramservicios.com.ar" sin "sistema."
     * pasa a "ramservicios.com.ar". Si no lo tiene, devuelve el mismo dominio.
     */
    public Dominio sinPrefijo(String prefijo) {
        if (valor.startsWith(prefijo) && valor.length() > prefijo.length()) {
            return new Dominio(valor.substring(prefijo.length()));
        }
        return this;
    }
}
