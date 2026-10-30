package com.ram.archivos;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchivoSubidoTest {

    private static final Set<TipoDeArchivo> COMPROBANTES = EnumSet.of(TipoDeArchivo.PDF, TipoDeArchivo.JPG, TipoDeArchivo.PNG);
    private static final Set<TipoDeArchivo> CVS = EnumSet.of(TipoDeArchivo.PDF, TipoDeArchivo.DOC, TipoDeArchivo.DOCX);

    static final byte[] PDF = "%PDF-1.7\n...contenido...".getBytes();
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};
    static final byte[] ZIP = {'P', 'K', 0x03, 0x04, 0, 0};
    /** Un ejecutable de Windows: empieza con "MZ". */
    static final byte[] EXE = {'M', 'Z', (byte) 0x90, 0, 3, 0};

    @Test
    void reconoceCadaTipoPorSusPrimerosBytes() {
        assertThat(ArchivoSubido.revisar("pago.pdf", PDF, COMPROBANTES).tipo()).isEqualTo(TipoDeArchivo.PDF);
        assertThat(ArchivoSubido.revisar("foto.jpeg", JPG, COMPROBANTES).tipo()).isEqualTo(TipoDeArchivo.JPG);
        assertThat(ArchivoSubido.revisar("captura.png", PNG, COMPROBANTES).tipo()).isEqualTo(TipoDeArchivo.PNG);
        assertThat(ArchivoSubido.revisar("cv.docx", ZIP, CVS).tipo()).isEqualTo(TipoDeArchivo.DOCX);
    }

    @Test
    void elNombreNoEngaña_unEjecutableRenombradoAPdfSeRechaza() {
        assertThatThrownBy(() -> ArchivoSubido.revisar("comprobante.pdf", EXE, COMPROBANTES))
                .isInstanceOf(ArchivoInvalidoException.class)
                .hasMessageContaining("PDF, JPG o PNG");
    }

    @Test
    void unPdfConNombreDeImagenSeAceptaComoPdf_yElNombreTomaLaExtensionReal() {
        ArchivoSubido archivo = ArchivoSubido.revisar("foto.jpg", PDF, COMPROBANTES);

        assertThat(archivo.tipo()).isEqualTo(TipoDeArchivo.PDF);
        assertThat(archivo.nombre()).isEqualTo("foto.pdf");
    }

    @Test
    void unZipSoloEsDocxSiSeLlamaPuntoDocx() {
        assertThatThrownBy(() -> ArchivoSubido.revisar("cv.zip", ZIP, CVS)).isInstanceOf(ArchivoInvalidoException.class);
    }

    @Test
    void soloAceptaLosTiposPermitidos() {
        assertThatThrownBy(() -> ArchivoSubido.revisar("foto.png", PNG, CVS))
                .isInstanceOf(ArchivoInvalidoException.class)
                .hasMessageContaining("PDF, DOC o DOCX");
    }

    @Test
    void rechazaVaciosYMasDe5Mb() {
        assertThatThrownBy(() -> ArchivoSubido.revisar("a.pdf", new byte[0], COMPROBANTES))
                .hasMessageContaining("vacío");

        byte[] grande = Arrays.copyOf(PDF, ArchivoSubido.TAMANO_MAXIMO + 1);
        assertThatThrownBy(() -> ArchivoSubido.revisar("a.pdf", grande, COMPROBANTES))
                .hasMessageContaining("5 MB");

        byte[] justo = Arrays.copyOf(PDF, ArchivoSubido.TAMANO_MAXIMO);
        assertThat(ArchivoSubido.revisar("a.pdf", justo, COMPROBANTES).tamano()).isEqualTo(ArchivoSubido.TAMANO_MAXIMO);
    }

    @Test
    void limpiaElNombre() {
        assertThat(ArchivoSubido.nombreLimpio("C:\\Users\\ana\\Mis CVs\\CV Ana López (final).PDF", TipoDeArchivo.PDF))
                .isEqualTo("CV Ana Lopez (final).pdf");
        assertThat(ArchivoSubido.nombreLimpio("../../etc/passwd", TipoDeArchivo.PDF)).isEqualTo("passwd.pdf");
        assertThat(ArchivoSubido.nombreLimpio("pago\r\nBcc: alguien@x.com.png", TipoDeArchivo.PNG))
                .isEqualTo("pagoBcc alguienx.com.png");
        assertThat(ArchivoSubido.nombreLimpio("", TipoDeArchivo.JPG)).isEqualTo("archivo.jpg");
        assertThat(ArchivoSubido.nombreLimpio("...", TipoDeArchivo.JPG)).isEqualTo("archivo.jpg");
        assertThat(ArchivoSubido.nombreLimpio("😀😀.pdf", TipoDeArchivo.PDF)).isEqualTo("archivo.pdf");
    }
}
