package com.ram.catalogo;

/** Una foto lista para mandar al navegador (siempre JPEG). */
public record ArchivoDeFoto(byte[] jpeg) {

    @Override
    public String toString() {
        return "ArchivoDeFoto[" + jpeg.length + " bytes]";
    }
}
