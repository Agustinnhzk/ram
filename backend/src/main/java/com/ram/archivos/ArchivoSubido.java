package com.ram.archivos;

import java.text.Normalizer;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Un archivo que subió alguien, ya revisado: no está vacío, no supera el tamaño máximo y es de un tipo permitido
 * según su firma. Si existe un {@code ArchivoSubido}, es válido.
 *
 * @param nombre nombre "limpio" para mostrar y para el adjunto: sin carpetas ni caracteres raros,
 *               y con la extensión del tipo REAL (un .exe renombrado a .pdf no llega hasta acá).
 */
public record ArchivoSubido(String nombre, TipoDeArchivo tipo, byte[] contenido) {

    /** 5 MB: alcanza para un CV o la foto de un comprobante, y frena a quien quiera llenar el disco. */
    public static final int TAMANO_MAXIMO = 5 * 1024 * 1024;
    /** Fotos de productos y banners: las del celular pesan más, y se achican al guardarlas. */
    public static final int TAMANO_MAXIMO_FOTO = 10 * 1024 * 1024;
    private static final int LARGO_MAXIMO_DEL_NOMBRE = 100;

    public ArchivoSubido {
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(contenido);
    }

    /**
     * Revisa un archivo recién recibido.
     *
     * @param nombreOriginal el nombre que mandó el navegador (puede ser cualquier cosa, incluso null)
     * @throws ArchivoInvalidoException con un mensaje para la persona
     */
    public static ArchivoSubido revisar(String nombreOriginal, byte[] contenido, Set<TipoDeArchivo> permitidos) {
        return revisar(nombreOriginal, contenido, permitidos, TAMANO_MAXIMO);
    }

    /** Igual, con otro tamaño máximo (las fotos de productos admiten hasta 10 MB: se achican al guardarlas). */
    public static ArchivoSubido revisar(String nombreOriginal, byte[] contenido, Set<TipoDeArchivo> permitidos, int tamanoMaximo) {
        if (contenido == null || contenido.length == 0) {
            throw new ArchivoInvalidoException("El archivo está vacío");
        }
        if (contenido.length > tamanoMaximo) {
            throw new ArchivoInvalidoException("El archivo no puede superar los " + tamanoMaximo / (1024 * 1024) + " MB");
        }
        String nombre = nombreOriginal == null ? "" : nombreOriginal;
        TipoDeArchivo tipo = TipoDeArchivo.detectar(contenido, nombre)
                .filter(permitidos::contains)
                .orElseThrow(() -> new ArchivoInvalidoException("El archivo tiene que ser " + nombres(permitidos)));
        return new ArchivoSubido(nombreLimpio(nombre, tipo), tipo, contenido);
    }

    public int tamano() {
        return contenido.length;
    }

    /**
     * "C:\Users\ana\Mis CVs\CV Ana López (final).PDF" → "CV Ana Lopez (final).pdf".
     * Saca la carpeta (algunos navegadores viejos la mandan), las tildes y todo lo que no sea letra, número,
     * espacio, punto, guion o paréntesis. Así el nombre es seguro en un encabezado de mail o de descarga.
     */
    static String nombreLimpio(String original, TipoDeArchivo tipo) {
        String base = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1);
        int punto = base.lastIndexOf('.');
        if (punto > 0) {
            base = base.substring(0, punto);
        }
        base = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 ._()-]", "")
                .replaceAll("\\s+", " ")
                .strip();
        if (base.isEmpty() || base.chars().allMatch(c -> c == '.')) {
            base = "archivo";
        }
        if (base.length() > LARGO_MAXIMO_DEL_NOMBRE) {
            base = base.substring(0, LARGO_MAXIMO_DEL_NOMBRE).strip();
        }
        return base + "." + tipo.extension();
    }

    private static String nombres(Set<TipoDeArchivo> tipos) {
        return tipos.stream().sorted().map(t -> t.name()).collect(Collectors.joining(", ", "", ""))
                .replaceFirst(", ([A-Z]+)$", " o $1");
    }

    /** Sin los bytes: si un log imprime el archivo, que no vuelque 5 MB. */
    @Override
    public String toString() {
        return "ArchivoSubido[" + nombre + ", " + tipo + ", " + contenido.length + " bytes]";
    }
}
