package com.ram.vidriera.dominio;

import com.ram.vidriera.BannerNuevo;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Un banner de la portada, con sus reglas. */
public final class Banner {

    private final UUID id;
    private String textoAlternativo;
    private String enlace;
    private String imagenHeredada;
    private long version;
    private boolean activo;
    private LocalDate desde;
    private LocalDate hasta;
    private int orden;

    public Banner(UUID id, String textoAlternativo, String enlace, String imagenHeredada, long version, boolean activo,
                  LocalDate desde, LocalDate hasta, int orden) {
        this.id = Objects.requireNonNull(id);
        this.textoAlternativo = textoAlternativo;
        this.enlace = enlace;
        this.imagenHeredada = imagenHeredada;
        this.version = version;
        this.activo = activo;
        this.desde = desde;
        this.hasta = hasta;
        this.orden = orden;
    }

    public static Banner nuevo(BannerNuevo datos, int orden, long version) {
        Banner banner = new Banner(UUID.randomUUID(), null, null, null, version, false, null, null, orden);
        banner.editar(datos);
        return banner;
    }

    public void editar(BannerNuevo datos) {
        String texto = datos.textoAlternativo() == null ? "" : datos.textoAlternativo().strip();
        if (texto.isEmpty() || texto.length() > 200) {
            throw new IllegalArgumentException("Escribí qué dice el banner (hasta 200 caracteres): lo leen quienes no ven la imagen");
        }
        if (datos.desde() != null && datos.hasta() != null && datos.hasta().isBefore(datos.desde())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }
        this.textoAlternativo = texto;
        this.enlace = enlaceSeguro(datos.enlace());
        this.activo = datos.activo();
        this.desde = datos.desde();
        this.hasta = datos.hasta();
    }

    /**
     * Solo rutas de la propia tienda ("/productos?categoria=camaras"). Nada de "javascript:..." ni de mandar a
     * otro sitio con "//otro.com": un banner es un link que la gente toca sin pensar.
     */
    static String enlaceSeguro(String enlace) {
        String limpio = enlace == null ? "" : enlace.strip();
        if (limpio.isEmpty()) {
            return "/productos";
        }
        if (!limpio.startsWith("/") || limpio.startsWith("//") || limpio.startsWith("/\\") || limpio.length() > 300
                || limpio.chars().anyMatch(c -> c < 0x20 || c == ' ')) {
            throw new IllegalArgumentException("El link tiene que ser una página de la tienda, por ejemplo /productos?categoria=camaras");
        }
        return limpio;
    }

    public boolean visible(LocalDate hoy) {
        return activo && (desde == null || !hoy.isBefore(desde)) && (hasta == null || !hoy.isAfter(hasta));
    }

    /** Se subió una imagen: desde ahora se usa esa (y deja de usarse la heredada, si tenía). */
    public void nuevaImagen(long version) {
        this.version = version;
        this.imagenHeredada = null;
    }

    public void moverA(int orden) {
        this.orden = orden;
    }

    public UUID id() {
        return id;
    }

    public String textoAlternativo() {
        return textoAlternativo;
    }

    public String enlace() {
        return enlace;
    }

    public String imagenHeredada() {
        return imagenHeredada;
    }

    public long version() {
        return version;
    }

    public boolean activo() {
        return activo;
    }

    public LocalDate desde() {
        return desde;
    }

    public LocalDate hasta() {
        return hasta;
    }

    public int orden() {
        return orden;
    }
}
