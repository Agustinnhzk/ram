package com.ram.catalogo.dominio;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/** La parte de la dirección: "Kit 4 cámaras + DVR" → "kit-4-camaras-dvr". */
public final class Slug {

    private static final int LARGO_MAXIMO = 80;

    private Slug() {
    }

    public static String de(String texto) {
        String base = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (base.length() > LARGO_MAXIMO) {
            base = base.substring(0, LARGO_MAXIMO).replaceAll("-+$", "");
        }
        return base.isEmpty() ? "producto" : base;
    }

    /** Si ya existe, le agrega -2, -3... hasta encontrar uno libre. */
    public static String libre(String texto, Predicate<String> existe) {
        String base = de(texto);
        String candidato = base;
        for (int n = 2; existe.test(candidato); n++) {
            candidato = base + "-" + n;
        }
        return candidato;
    }
}
