package com.ram.archivos;

/** Una imagen ya preparada para la web: JPEG, achicada y derecha. */
public record ImagenLista(byte[] jpeg, int ancho, int alto) {

    @Override
    public String toString() {
        return "ImagenLista[" + ancho + "x" + alto + ", " + jpeg.length + " bytes]";
    }
}
