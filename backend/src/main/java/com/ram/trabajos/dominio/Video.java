package com.ram.trabajos.dominio;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * El link de un video de un trabajo: un reel o post de Instagram, o un video de YouTube (también Shorts).
 *
 * <p>Se acepta como lo copia el celular (con "?igsh=...", "&amp;t=30s", "m.youtube.com", "youtu.be") y se guarda limpio,
 * siempre de la misma forma, para que la tienda sepa cómo mostrarlo:
 * <ul>
 *   <li>{@code https://www.instagram.com/reel/ABC123/}</li>
 *   <li>{@code https://www.youtube.com/watch?v=abcdefghijk}</li>
 * </ul>
 * Cualquier otro sitio se rechaza: la tienda solo sabe mostrar esos dos.
 */
public final class Video {

    private static final Pattern INSTAGRAM = Pattern.compile("^/(?:[A-Za-z0-9_.]+/)?(reel|reels|p|tv)/([A-Za-z0-9_-]{5,40})/?$");
    private static final Pattern YOUTUBE_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern YOUTUBE_RUTA = Pattern.compile("^/(?:shorts|embed|live)/([A-Za-z0-9_-]{11})/?$");
    private static final Pattern YOUTUBE_V = Pattern.compile("(?:^|&)v=([A-Za-z0-9_-]{11})(?:&|$)");

    static final String ERROR = "Pegá el link de un reel de Instagram o de un video de YouTube";

    private Video() {
    }

    /** null o vacío → sin video (null). Si no es de Instagram o YouTube → IllegalArgumentException. */
    public static String limpiar(String link) {
        String texto = link == null ? "" : link.strip();
        if (texto.isEmpty()) {
            return null;
        }
        if (!texto.matches("(?i)^https?://.*")) {
            texto = "https://" + texto;
        }
        URI uri;
        try {
            uri = URI.create(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ERROR);
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT).replaceFirst("^(www\\.|m\\.)", "");
        String ruta = uri.getRawPath() == null ? "" : uri.getRawPath();

        if (host.equals("instagram.com")) {
            Matcher m = INSTAGRAM.matcher(ruta);
            if (m.matches()) {
                String tipo = m.group(1).equals("reels") ? "reel" : m.group(1);
                return "https://www.instagram.com/" + tipo + "/" + m.group(2) + "/";
            }
        }
        if (host.equals("youtu.be")) {
            String id = ruta.replaceFirst("^/", "").replaceFirst("/$", "");
            if (YOUTUBE_ID.matcher(id).matches()) {
                return youtube(id);
            }
        }
        if (host.equals("youtube.com") || host.equals("music.youtube.com")) {
            Matcher porRuta = YOUTUBE_RUTA.matcher(ruta);
            if (porRuta.matches()) {
                return youtube(porRuta.group(1));
            }
            if (ruta.equals("/watch") && uri.getRawQuery() != null) {
                Matcher v = YOUTUBE_V.matcher(uri.getRawQuery());
                if (v.find()) {
                    return youtube(v.group(1));
                }
            }
        }
        throw new IllegalArgumentException(ERROR);
    }

    private static String youtube(String id) {
        return "https://www.youtube.com/watch?v=" + id;
    }
}
