package com.ram.archivos;

import java.util.Arrays;
import java.util.Optional;

/**
 * Los tipos de archivo que acepta el sistema, con la firma (primeros bytes) de cada uno.
 *
 * <p>DOCX es un ZIP por dentro: su firma es la de cualquier ZIP. Por eso, para DOCX se exige además
 * que el nombre termine en .docx. Un ZIP renombrado pasaría el control, pero no se ejecuta ni se muestra:
 * solo se adjunta al mail, y el que lo abre es Word.
 */
public enum TipoDeArchivo {

    PDF("application/pdf", "pdf", new byte[]{'%', 'P', 'D', 'F', '-'}),
    JPG("image/jpeg", "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
    PNG("image/png", "png", new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}),
    /** Word viejo (.doc): formato OLE de Microsoft. */
    DOC("application/msword", "doc", new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1}),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", new byte[]{'P', 'K', 0x03, 0x04});

    private final String tipoDeContenido;
    private final String extension;
    private final byte[] firma;

    TipoDeArchivo(String tipoDeContenido, String extension, byte[] firma) {
        this.tipoDeContenido = tipoDeContenido;
        this.extension = extension;
        this.firma = firma;
    }

    /** El Content-Type correcto para este tipo (el que se usa al descargarlo o adjuntarlo, nunca el que mandó el navegador). */
    public String tipoDeContenido() {
        return tipoDeContenido;
    }

    public String extension() {
        return extension;
    }

    /** El tipo real del archivo según sus primeros bytes, o vacío si no es ninguno de los conocidos. */
    public static Optional<TipoDeArchivo> detectar(byte[] contenido, String nombre) {
        return Arrays.stream(values())
                .filter(t -> t.coincide(contenido))
                .filter(t -> t != DOCX || nombre.toLowerCase().endsWith(".docx"))
                .findFirst();
    }

    private boolean coincide(byte[] contenido) {
        if (contenido.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if (contenido[i] != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
