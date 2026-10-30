package com.ram.pedidos;

/** El archivo de un comprobante, para descargarlo. */
public record ArchivoDeComprobante(String nombre, String tipoDeContenido, byte[] contenido) {

    @Override
    public String toString() {
        return "ArchivoDeComprobante[" + nombre + ", " + contenido.length + " bytes]";
    }
}
