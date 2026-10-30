package com.ram.archivos;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagenesTest {

    private static final Set<TipoDeArchivo> IMAGENES = Set.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG);

    @Test
    void achicaManteniendoLaProporcion_yDevuelveJpeg() throws IOException {
        ArchivoSubido png = ArchivoSubido.revisar("foto.png", imagen("png", 3000, 1500), IMAGENES);

        ImagenLista lista = Imagenes.preparar(png, 1600);

        assertThat(lista.ancho()).isEqualTo(1600);
        assertThat(lista.alto()).isEqualTo(800);
        assertThat(TipoDeArchivo.detectar(lista.jpeg(), "x").orElseThrow()).isEqualTo(TipoDeArchivo.JPG);
        BufferedImage leida = ImageIO.read(new ByteArrayInputStream(lista.jpeg()));
        assertThat(leida.getWidth()).isEqualTo(1600);
    }

    @Test
    void unaChicaNoSeAgranda() throws IOException {
        ImagenLista lista = Imagenes.preparar(ArchivoSubido.revisar("a.jpg", imagen("jpg", 400, 300), IMAGENES), 1600);

        assertThat(lista.ancho()).isEqualTo(400);
        assertThat(lista.alto()).isEqualTo(300);
    }

    @Test
    void leeLaOrientacionDelExif_yEndereza() throws IOException {
        byte[] acostada = conOrientacion(imagen("jpg", 400, 200), 6);
        assertThat(Imagenes.orientacionExif(acostada)).isEqualTo(6);
        assertThat(Imagenes.orientacionExif(conOrientacionBigEndian(imagen("jpg", 40, 20), 8))).isEqualTo(8);

        ImagenLista lista = Imagenes.preparar(ArchivoSubido.revisar("cel.jpg", acostada, IMAGENES), 1600);
        assertThat(lista.ancho()).isEqualTo(200);
        assertThat(lista.alto()).isEqualTo(400);
    }

    @Test
    void sinExifOConExifRotoQuedaComoEsta() throws IOException {
        assertThat(Imagenes.orientacionExif(imagen("jpg", 10, 10))).isEqualTo(1);
        byte[] roto = conOrientacion(imagen("jpg", 10, 10), 6);
        byte[] recortado = java.util.Arrays.copyOf(roto, 30);
        assertThat(Imagenes.orientacionExif(recortado)).isEqualTo(1);
    }

    @Test
    void lasOchoOrientacionesDejanCadaPixelDondeVa() {
        int w = 3, h = 2;
        BufferedImage base = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                base.setRGB(x, y, (x * 60) << 16 | (y * 120) << 8 | 7);
            }
        }
        for (int o = 1; o <= 8; o++) {
            BufferedImage r = Imagenes.enderezar(base, o);
            boolean gira = o >= 5;
            assertThat(r.getWidth()).isEqualTo(gira ? h : w);
            for (int y = 0; y < r.getHeight(); y++) {
                for (int x = 0; x < r.getWidth(); x++) {
                    int[] origen = switch (o) {
                        case 1 -> new int[]{x, y};
                        case 2 -> new int[]{w - 1 - x, y};
                        case 3 -> new int[]{w - 1 - x, h - 1 - y};
                        case 4 -> new int[]{x, h - 1 - y};
                        case 5 -> new int[]{y, x};
                        case 6 -> new int[]{y, h - 1 - x};
                        case 7 -> new int[]{w - 1 - y, h - 1 - x};
                        default -> new int[]{w - 1 - y, x};
                    };
                    assertThat(r.getRGB(x, y) & 0xFFFFFF).as("orientación " + o + " en " + x + "," + y)
                            .isEqualTo(base.getRGB(origen[0], origen[1]) & 0xFFFFFF);
                }
            }
        }
    }

    @Test
    void rechazaLoQueNoEsUnaImagenQueSePuedaAbrir() {
        byte[] falso = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 1, 2, 3};
        assertThatThrownBy(() -> Imagenes.preparar(ArchivoSubido.revisar("a.jpg", falso, IMAGENES), 1600))
                .isInstanceOf(ArchivoInvalidoException.class);
        ArchivoSubido pdf = ArchivoSubido.revisar("a.pdf", "%PDF-1.4".getBytes(), Set.of(TipoDeArchivo.PDF));
        assertThatThrownBy(() -> Imagenes.preparar(pdf, 1600)).isInstanceOf(ArchivoInvalidoException.class);
    }

    static byte[] imagen(String formato, int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, formato.equals("jpg") ? "jpeg" : formato, salida);
        return salida.toByteArray();
    }

    /** Mete un segmento EXIF (little endian, "II") con la orientación, justo después del inicio del JPEG. */
    static byte[] conOrientacion(byte[] jpeg, int orientacion) {
        byte[] tiff = {'I', 'I', 42, 0, 8, 0, 0, 0, 1, 0, 0x12, 0x01, 3, 0, 1, 0, 0, 0, (byte) orientacion, 0, 0, 0, 0, 0, 0, 0};
        return insertar(jpeg, tiff);
    }

    static byte[] conOrientacionBigEndian(byte[] jpeg, int orientacion) {
        byte[] tiff = {'M', 'M', 0, 42, 0, 0, 0, 8, 0, 1, 0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, (byte) orientacion, 0, 0, 0, 0, 0, 0};
        return insertar(jpeg, tiff);
    }

    private static byte[] insertar(byte[] jpeg, byte[] tiff) {
        byte[] exif = new byte[4 + 6 + tiff.length];
        int largo = exif.length - 2;
        exif[0] = (byte) 0xFF;
        exif[1] = (byte) 0xE1;
        exif[2] = (byte) (largo >> 8);
        exif[3] = (byte) largo;
        System.arraycopy("Exif\0\0".getBytes(), 0, exif, 4, 6);
        System.arraycopy(tiff, 0, exif, 10, tiff.length);
        byte[] resultado = new byte[jpeg.length + exif.length];
        System.arraycopy(jpeg, 0, resultado, 0, 2);
        System.arraycopy(exif, 0, resultado, 2, exif.length);
        System.arraycopy(jpeg, 2, resultado, 2 + exif.length, jpeg.length - 2);
        return resultado;
    }
}
