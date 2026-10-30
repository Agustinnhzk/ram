package com.ram.archivos;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Prepara las fotos que se suben (productos, banners) para mostrarlas en la web:
 * <ul>
 *   <li><b>Las endereza:</b> el celular guarda la foto "acostada" y anota en el EXIF cómo girarla.
 *       Java no lee ese dato, así que sin esto las fotos del celular aparecen de costado.</li>
 *   <li><b>Las achica:</b> una foto de 4000 px pesa 4 MB; a 1600 px como JPEG, unos 200 KB.</li>
 *   <li><b>Las pasa a JPEG</b> (si eran PNG con transparencia, sobre fondo blanco). Así se guarda un solo formato.</li>
 * </ul>
 * Usa solo lo que trae Java (ImageIO): sin librerías nativas. WebP no se genera acá: el navegador recibe JPEG.
 */
public final class Imagenes {

    /** 50 megapíxeles. Más es sospechoso: una imagen chica en bytes pero enorme al abrirla puede llenar la memoria. */
    static final long PIXELES_MAXIMOS = 50_000_000L;
    private static final float CALIDAD = 0.85f;

    private Imagenes() {
    }

    /**
     * @param archivo     ya revisado (JPG o PNG)
     * @param ladoMaximo  el lado más largo del resultado, en píxeles (si es más chica, no se agranda)
     * @throws ArchivoInvalidoException si no es una imagen que se pueda abrir
     */
    public static ImagenLista preparar(ArchivoSubido archivo, int ladoMaximo) {
        if (archivo.tipo() != TipoDeArchivo.JPG && archivo.tipo() != TipoDeArchivo.PNG) {
            throw new ArchivoInvalidoException("La imagen tiene que ser JPG o PNG");
        }
        BufferedImage original = leer(archivo.contenido(), ladoMaximo);
        int orientacion = archivo.tipo() == TipoDeArchivo.JPG ? orientacionExif(archivo.contenido()) : 1;
        BufferedImage derecha = enderezar(original, orientacion);
        BufferedImage achicada = achicar(derecha, ladoMaximo);
        return new ImagenLista(aJpeg(achicada), achicada.getWidth(), achicada.getHeight());
    }

    /**
     * Lee la imagen. Antes de abrirla entera, mira el tamaño (es gratis: está en el encabezado):
     * si es gigante, se rechaza; si es mucho más grande que lo que se necesita, se lee salteando píxeles
     * (subsampling), que usa mucha menos memoria.
     */
    private static BufferedImage leer(byte[] bytes, int ladoMaximo) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw new ArchivoInvalidoException("No se pudo abrir la imagen");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(entrada, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if ((long) ancho * alto > PIXELES_MAXIMOS) {
                    throw new ArchivoInvalidoException("La imagen es demasiado grande (más de 50 megapíxeles)");
                }
                ImageReadParam parametros = lector.getDefaultReadParam();
                int salto = Math.max(1, Math.max(ancho, alto) / (ladoMaximo * 2));
                parametros.setSourceSubsampling(salto, salto, 0, 0);
                return lector.read(0, parametros);
            } finally {
                lector.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof ArchivoInvalidoException invalido) {
                throw invalido;
            }
            throw new ArchivoInvalidoException("No se pudo abrir la imagen: puede estar dañada");
        }
    }

    private static BufferedImage achicar(BufferedImage imagen, int ladoMaximo) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        double escala = Math.min(1.0, (double) ladoMaximo / Math.max(ancho, alto));
        int nuevoAncho = Math.max(1, (int) Math.round(ancho * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * escala));
        BufferedImage resultado = new BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultado.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, nuevoAncho, nuevoAlto);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(imagen, 0, 0, nuevoAncho, nuevoAlto, null);
        } finally {
            g.dispose();
        }
        return resultado;
    }

    /** Las 8 orientaciones del EXIF: 1 = derecha; 3 = dada vuelta; 6 = girar 90° a la derecha; 8 = a la izquierda; 2, 4, 5, 7 = espejadas. */
    static BufferedImage enderezar(BufferedImage imagen, int orientacion) {
        if (orientacion < 2 || orientacion > 8) {
            return imagen;
        }
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        boolean gira = orientacion >= 5;
        AffineTransform t = new AffineTransform();
        switch (orientacion) {
            case 2 -> { t.translate(ancho, 0); t.scale(-1, 1); }
            case 3 -> { t.translate(ancho, alto); t.rotate(Math.PI); }
            case 4 -> { t.translate(0, alto); t.scale(1, -1); }
            case 5 -> { t.rotate(Math.PI / 2); t.scale(1, -1); }
            case 6 -> { t.translate(alto, 0); t.rotate(Math.PI / 2); }
            case 7 -> { t.scale(-1, 1); t.translate(-alto, 0); t.translate(0, ancho); t.rotate(3 * Math.PI / 2); }
            case 8 -> { t.translate(0, ancho); t.rotate(3 * Math.PI / 2); }
            default -> { }
        }
        BufferedImage resultado = new BufferedImage(gira ? alto : ancho, gira ? ancho : alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultado.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, resultado.getWidth(), resultado.getHeight());
            g.drawImage(imagen, t, null);
        } finally {
            g.dispose();
        }
        return resultado;
    }

    /**
     * Busca en el JPEG el dato "Orientation" (etiqueta 0x0112) del bloque EXIF (segmento APP1).
     * Cualquier cosa rara (sin EXIF, recortado, mal formado) → 1: se deja como está.
     */
    static int orientacionExif(byte[] jpeg) {
        try {
            int i = 2; // después de FF D8
            while (i + 4 <= jpeg.length) {
                if ((jpeg[i] & 0xFF) != 0xFF) {
                    return 1;
                }
                int marcador = jpeg[i + 1] & 0xFF;
                if (marcador == 0xDA || marcador == 0xD9) {
                    return 1; // empiezan los datos de la imagen: no hubo EXIF
                }
                int largo = ((jpeg[i + 2] & 0xFF) << 8) | (jpeg[i + 3] & 0xFF);
                if (marcador == 0xE1 && largo >= 8 && i + 10 <= jpeg.length
                        && new String(jpeg, i + 4, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("Exif")) {
                    return leerOrientacionTiff(jpeg, i + 10, Math.min(jpeg.length, i + 2 + largo));
                }
                i += 2 + largo;
            }
        } catch (RuntimeException e) {
            return 1;
        }
        return 1;
    }

    private static int leerOrientacionTiff(byte[] b, int inicio, int fin) {
        boolean little = b[inicio] == 'I';
        int ifd = inicio + leer32(b, inicio + 4, little);
        int entradas = leer16(b, ifd, little);
        for (int e = 0; e < entradas; e++) {
            int entrada = ifd + 2 + e * 12;
            if (entrada + 12 > fin) {
                return 1;
            }
            if (leer16(b, entrada, little) == 0x0112) {
                int valor = leer16(b, entrada + 8, little);
                return valor >= 1 && valor <= 8 ? valor : 1;
            }
        }
        return 1;
    }

    private static int leer16(byte[] b, int i, boolean little) {
        return little ? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8) : ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
    }

    private static int leer32(byte[] b, int i, boolean little) {
        return little
                ? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8) | ((b[i + 2] & 0xFF) << 16) | ((b[i + 3] & 0xFF) << 24)
                : ((b[i] & 0xFF) << 24) | ((b[i + 1] & 0xFF) << 16) | ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);
    }

    private static byte[] aJpeg(BufferedImage imagen) {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ImageOutputStream destino = ImageIO.createImageOutputStream(salida)) {
            escritor.setOutput(destino);
            ImageWriteParam parametros = escritor.getDefaultWriteParam();
            parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parametros.setCompressionQuality(CALIDAD);
            escritor.write(null, new IIOImage(imagen, null, null), parametros);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el JPEG", e);
        } finally {
            escritor.dispose();
        }
        return salida.toByteArray();
    }
}
