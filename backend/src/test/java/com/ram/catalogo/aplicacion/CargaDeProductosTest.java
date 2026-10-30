package com.ram.catalogo.aplicacion;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FichaDeProducto;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.SinPermisoException;
import com.ram.catalogo.TipoComponente;
import com.ram.catalogo.TipoRam;
import com.ram.catalogo.dominio.Categoria;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Carga de productos desde el sistema: alta, edición, ofertas, fotos y categorías (ADR-013). */
class CargaDeProductosTest {

    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    /** 29/10/2026 a las 23:30 de Argentina = 30/10 02:30 UTC: "hoy" tiene que ser el 29. */
    private static final Instant AHORA = Instant.parse("2026-10-30T02:30:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 29);

    private final CatalogoEnMemoria memoria = new CatalogoEnMemoria();
    private final Categoria camaras = new Categoria(UUID.randomUUID(), "camaras", "Cámaras", true);
    private final Categoria componentes = new Categoria(UUID.randomUUID(), "componentes", "Componentes", false);
    private UsuarioAutenticado logueado = usuario(Rol.DUENO);
    private ServicioCatalogo servicio;

    @BeforeEach
    void preparar() {
        memoria.agregar(camaras);
        memoria.agregar(componentes);
        servicio = new ServicioCatalogo(memoria, memoria, memoria, () -> Optional.ofNullable(logueado),
                Clock.fixed(AHORA, ZoneOffset.UTC), ARGENTINA);
    }

    @Test
    void elDuenoCargaUnProducto_conSlugAutomaticoYPrecioEnElHistorial() {
        ProductoId id = servicio.crearProducto(ficha("Cámara Wi-Fi 360°", "camaras", true, null), 45_000L);

        DatosProducto p = servicio.obtener(id).orElseThrow();
        assertThat(p.slug()).isEqualTo("camara-wi-fi-360");
        assertThat(p.precio()).isEqualTo(45_000L);
        assertThat(p.caracteristicas()).containsExactly("Full HD", "Visión nocturna");
        assertThat(memoria.cambios()).hasSize(1);
        assertThat(servicio.buscar(new FiltroCatalogo("camaras", null, "wi-fi"))).hasSize(1);
    }

    @Test
    void dosProductosConElMismoNombreTienenDistintoSlug() {
        servicio.crearProducto(ficha("DVR 8 canales", "camaras", true, null), null);
        ProductoId segundo = servicio.crearProducto(ficha("DVR 8 canales", "camaras", true, null), null);

        assertThat(servicio.obtener(segundo).orElseThrow().slug()).isEqualTo("dvr-8-canales-2");
        assertThat(memoria.cambios()).as("sin precio no hay historial").isEmpty();
    }

    @Test
    void alEditarCambiaTodoMenosElSlug() {
        ProductoId id = servicio.crearProducto(ficha("DVR 8 canales", "camaras", true, null), 100_000L);
        servicio.editarProducto(id, ficha("DVR 16 canales Hikvision", "componentes", false, null));

        DatosProducto p = servicio.obtener(id).orElseThrow();
        assertThat(p.nombre()).isEqualTo("DVR 16 canales Hikvision");
        assertThat(p.slug()).isEqualTo("dvr-8-canales");
        assertThat(p.categoria().slug()).isEqualTo("componentes");
        assertThat(p.publicado()).isFalse();
        assertThat(p.precio()).as("el precio no se toca al editar").isEqualTo(100_000L);
    }

    @Test
    void validaLaFicha() {
        assertThatThrownBy(() -> servicio.crearProducto(ficha("  ", "camaras", true, null), null)).hasMessageContaining("nombre");
        assertThatThrownBy(() -> servicio.crearProducto(ficha("X", "no-existe", true, null), null)).hasMessageContaining("categoría");
        assertThatThrownBy(() -> servicio.crearProducto(new FichaDeProducto("X", "camaras", null, "", "", List.of(), null, true, null), null))
                .hasMessageContaining("condición");
        assertThatThrownBy(() -> servicio.crearProducto(new FichaDeProducto("X", "camaras", Condicion.NUEVO, "", "", List.of(), 0, true, null), null))
                .hasMessageContaining("garantía");
    }

    @Test
    void unComponenteTieneQueTraerSusDatosTecnicos_ySeDescartanLosQueNoLeCorresponden() {
        DatosTecnicos cpuSinSocket = new DatosTecnicos(TipoComponente.CPU, null, null, null, List.of(), 65, null, false);
        assertThatThrownBy(() -> servicio.crearProducto(ficha("Ryzen", "componentes", false, cpuSinSocket), null))
                .hasMessageContaining("socket");

        DatosTecnicos fuenteConBasura = new DatosTecnicos(TipoComponente.FUENTE, "AM4", TipoRam.DDR4, FormatoMother.ATX, List.of(), 100, 650, true);
        ProductoId fuente = servicio.crearProducto(ficha("Fuente 650 W", "componentes", false, fuenteConBasura), 90_000L);
        DatosTecnicos guardados = servicio.obtener(fuente).orElseThrow().tecnicos();
        assertThat(guardados.potenciaW()).isEqualTo(650);
        assertThat(guardados.socket()).isNull();
        assertThat(guardados.consumoW()).isNull();
        assertThat(servicio.componentes()).hasSize(1);

        DatosTecnicos mother = new DatosTecnicos(TipoComponente.MOTHER, " am5 ", TipoRam.DDR5, FormatoMother.MATX, List.of(), null, null, false);
        ProductoId id = servicio.crearProducto(ficha("Mother A620M", "componentes", false, mother), null);
        assertThat(servicio.obtener(id).orElseThrow().tecnicos().socket()).isEqualTo("AM5");
    }

    @Test
    void laOfertaValeHastaElDiaQueDice_enHoraArgentina() {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), 100_000L);
        servicio.cambiarOferta(id, 80_000L, HOY);

        DatosProducto hoy = servicio.obtener(id).orElseThrow();
        assertThat(hoy.precioVigente()).isEqualTo(80_000L);
        assertThat(hoy.enOferta()).isTrue();

        // Al día siguiente (otro servicio con el reloj adelantado), ya no.
        ServicioCatalogo manana = new ServicioCatalogo(memoria, memoria, memoria, () -> Optional.of(logueado),
                Clock.fixed(AHORA.plusSeconds(3600), ZoneOffset.UTC), ARGENTINA);
        DatosProducto despues = manana.obtener(id).orElseThrow();
        assertThat(despues.precioVigente()).isEqualTo(100_000L);
        assertThat(despues.enOferta()).isFalse();
    }

    @Test
    void reglasDeLaOferta() {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), 100_000L);
        assertThatThrownBy(() -> servicio.cambiarOferta(id, 100_000L, HOY)).hasMessageContaining("menor");
        assertThatThrownBy(() -> servicio.cambiarOferta(id, 80_000L, HOY.minusDays(1))).hasMessageContaining("antes de hoy");
        assertThatThrownBy(() -> servicio.cambiarOferta(id, 80_000L, null)).hasMessageContaining("hasta cuándo");

        ProductoId sinPrecio = servicio.crearProducto(ficha("Otro", "camaras", true, null), null);
        assertThatThrownBy(() -> servicio.cambiarOferta(sinPrecio, 80_000L, HOY)).hasMessageContaining("precio de lista");

        servicio.cambiarOferta(id, 80_000L, HOY.plusDays(10));
        servicio.cambiarOferta(id, null, null);
        assertThat(servicio.obtener(id).orElseThrow().enOferta()).isFalse();
    }

    @Test
    void siDespuesSeBajaElPrecioDeListaGanaElMasBarato() {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), 100_000L);
        servicio.cambiarOferta(id, 80_000L, HOY.plusDays(10));
        servicio.cambiarPrecio(id, 70_000L);

        DatosProducto p = servicio.obtener(id).orElseThrow();
        assertThat(p.precioVigente()).isEqualTo(70_000L);
        assertThat(p.enOferta()).isFalse();
    }

    @Test
    void elAumentoNoTocaLaOferta() {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), 100_000L);
        servicio.cambiarOferta(id, 80_000L, HOY.plusDays(10));
        servicio.aplicarAumento(BigDecimal.TEN, null);

        DatosProducto p = servicio.obtener(id).orElseThrow();
        assertThat(p.precio()).isEqualTo(110_000L);
        assertThat(p.precioVigente()).isEqualTo(80_000L);
    }

    @Test
    void fotos_seAchican_seOrdenan_ySeBorran() throws IOException {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), null);
        UUID primera = servicio.agregarFoto(id, jpg(2000, 1000));
        UUID segunda = servicio.agregarFoto(id, jpg(300, 300));

        assertThat(servicio.obtener(id).orElseThrow().fotos()).containsExactly(primera, segunda);
        assertThat(ancho(servicio.foto(primera, false).orElseThrow().jpeg())).isEqualTo(1600);
        assertThat(ancho(servicio.foto(primera, true).orElseThrow().jpeg())).isEqualTo(600);

        servicio.ordenarFotos(id, List.of(segunda, primera));
        assertThat(servicio.obtener(id).orElseThrow().fotos()).containsExactly(segunda, primera);
        assertThatThrownBy(() -> servicio.ordenarFotos(id, List.of(segunda))).isInstanceOf(IllegalArgumentException.class);

        servicio.borrarFoto(id, segunda);
        assertThat(servicio.obtener(id).orElseThrow().fotos()).containsExactly(primera);
        assertThat(servicio.foto(segunda, false)).isEmpty();
    }

    @Test
    void hastaSeisFotos_yTienenQueSerImagenes() throws IOException {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), null);
        for (int i = 0; i < ServicioCatalogo.FOTOS_MAXIMAS; i++) {
            servicio.agregarFoto(id, jpg(10, 10));
        }
        assertThatThrownBy(() -> servicio.agregarFoto(id, jpg(10, 10))).hasMessageContaining("hasta 6");

        ProductoId otro = servicio.crearProducto(ficha("Otro", "camaras", true, null), null);
        ArchivoSubido pdf = ArchivoSubido.revisar("a.pdf", "%PDF-1.4".getBytes(), Set.of(TipoDeArchivo.PDF));
        assertThatThrownBy(() -> servicio.agregarFoto(otro, pdf)).isInstanceOf(ArchivoInvalidoException.class);
    }

    @Test
    void soloElDuenoCambiaElCatalogo() throws IOException {
        ProductoId id = servicio.crearProducto(ficha("Kit", "camaras", true, null), 100_000L);
        logueado = usuario(Rol.MOSTRADOR);

        assertThatThrownBy(() -> servicio.crearProducto(ficha("X", "camaras", true, null), null)).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.editarProducto(id, ficha("X", "camaras", true, null))).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.cambiarOferta(id, 1L, HOY)).isInstanceOf(SinPermisoException.class);
        ArchivoSubido foto = jpg(10, 10);
        assertThatThrownBy(() -> servicio.agregarFoto(id, foto)).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.crearCategoria("Redes", false)).isInstanceOf(SinPermisoException.class);
        assertThat(servicio.obtener(id)).isPresent(); // pero ver, sí
    }

    @Test
    void categorias_seCreanConSlug_yAlEditarElSlugNoCambia() {
        var redes = servicio.crearCategoria("Redes y Wi-Fi", false);
        assertThat(redes.slug()).isEqualTo("redes-y-wi-fi");
        assertThat(servicio.crearCategoria("Redes y Wi-Fi", true).slug()).isEqualTo("redes-y-wi-fi-2");

        servicio.editarCategoria("redes-y-wi-fi", "Redes", true);
        assertThat(servicio.categorias().stream().filter(c -> c.slug().equals("redes-y-wi-fi")).findFirst().orElseThrow().nombre())
                .isEqualTo("Redes");
        assertThatThrownBy(() -> servicio.crearCategoria(" ", false)).isInstanceOf(IllegalArgumentException.class);
    }

    private static FichaDeProducto ficha(String nombre, String categoria, boolean publicado, DatosTecnicos tecnicos) {
        return new FichaDeProducto(nombre, categoria, Condicion.NUEVO, "Resumen", "Descripción",
                List.of("Full HD", " ", "Visión nocturna"), 12, publicado, tecnicos);
    }

    private static ArchivoSubido jpg(int ancho, int alto) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), "jpeg", salida);
        return ArchivoSubido.revisar("foto.jpg", salida.toByteArray(), Set.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG));
    }

    private static int ancho(byte[] jpeg) throws IOException {
        return ImageIO.read(new java.io.ByteArrayInputStream(jpeg)).getWidth();
    }

    private static UsuarioAutenticado usuario(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín", TipoDeCuenta.PERSONAL, rol);
    }
}
