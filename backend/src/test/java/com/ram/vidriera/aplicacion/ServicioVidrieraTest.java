package com.ram.vidriera.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import com.ram.vidriera.BannerNuevo;
import com.ram.vidriera.DatosBanner;
import com.ram.vidriera.SinPermisoException;
import com.ram.vidriera.dominio.Banner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioVidrieraTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 29);

    private final BannersEnMemoria memoria = new BannersEnMemoria();
    private UsuarioAutenticado logueado = usuario(Rol.DUENO);
    private ServicioVidriera servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioVidriera(memoria, () -> Optional.ofNullable(logueado),
                Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Argentina/Buenos_Aires"));
    }

    @Test
    void laTiendaVeSoloLosActivosYDentroDeSusFechas_enOrden() throws IOException {
        UUID siempre = servicio.crear(datos("Siempre", true, null, null), jpg());
        servicio.crear(datos("Apagado", false, null, null), jpg());
        servicio.crear(datos("Empieza mañana", true, HOY.plusDays(1), null), jpg());
        servicio.crear(datos("Terminó ayer", true, null, HOY.minusDays(1)), jpg());
        UUID hastaHoy = servicio.crear(datos("Último día", true, HOY.minusDays(3), HOY), jpg());

        assertThat(servicio.paraLaTienda().stream().map(DatosBanner::id).toList()).containsExactly(siempre, hastaHoy);
        assertThat(servicio.todos()).hasSize(5);
    }

    @Test
    void elEnlaceSoloPuedeIrAUnaPaginaDeLaTienda() throws IOException {
        for (String malo : List.of("javascript:alert(1)", "https://otro.com", "//otro.com", "/\\\\otro.com", "/con espacio")) {
            assertThatThrownBy(() -> servicio.crear(new BannerNuevo("Hola", malo, true, null, null), jpg()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        UUID bien = servicio.crear(new BannerNuevo("Hola", "/productos?categoria=camaras", true, null, null), jpg());
        UUID sinLink = servicio.crear(new BannerNuevo("Hola", "", true, null, null), jpg());
        assertThat(servicio.todos().get(0).enlace()).isEqualTo("/productos?categoria=camaras");
        assertThat(memoria.porId(sinLink).orElseThrow().enlace()).isEqualTo("/productos");
        assertThat(bien).isNotEqualTo(sinLink);
    }

    @Test
    void textoAlternativoObligatorio_yFechasEnOrden() {
        assertThatThrownBy(() -> servicio.crear(datos(" ", true, null, null), jpg())).hasMessageContaining("qué dice el banner");
        assertThatThrownBy(() -> servicio.crear(datos("X", true, HOY, HOY.minusDays(1)), jpg())).hasMessageContaining("anterior");
    }

    @Test
    void cambiarLaImagenCambiaLaVersion_yDejaDeUsarLaHeredada() throws IOException {
        Banner heredado = new Banner(UUID.randomUUID(), "Flyer", "/", "/promos/flyer.jpg", 0, true, null, null, 0);
        memoria.guardar(heredado);

        servicio.cambiarImagen(heredado.id(), jpg());

        DatosBanner despues = servicio.todos().getFirst();
        assertThat(despues.imagenHeredada()).isNull();
        assertThat(despues.version()).isNotEqualTo(0L);
        assertThat(servicio.imagen(heredado.id())).isPresent();
    }

    @Test
    void ordenarYBorrar() throws IOException {
        UUID a = servicio.crear(datos("A", true, null, null), jpg());
        UUID b = servicio.crear(datos("B", true, null, null), jpg());
        UUID c = servicio.crear(datos("C", true, null, null), jpg());

        servicio.ordenar(List.of(c, a, b));
        assertThat(servicio.todos().stream().map(DatosBanner::id).toList()).containsExactly(c, a, b);
        assertThatThrownBy(() -> servicio.ordenar(List.of(c, a))).isInstanceOf(IllegalArgumentException.class);

        servicio.borrar(a);
        assertThat(servicio.todos().stream().map(DatosBanner::orden).toList()).containsExactly(0, 1);
        assertThat(servicio.imagen(a)).isEmpty();
    }

    @Test
    void soloElDueno() {
        logueado = usuario(Rol.MOSTRADOR);
        assertThatThrownBy(() -> servicio.crear(datos("X", true, null, null), jpg())).isInstanceOf(SinPermisoException.class);
    }

    private static BannerNuevo datos(String texto, boolean activo, LocalDate desde, LocalDate hasta) {
        return new BannerNuevo(texto, "/productos", activo, desde, hasta);
    }

    private static ArchivoSubido jpg() {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB), "jpeg", salida);
            return ArchivoSubido.revisar("b.jpg", salida.toByteArray(), Set.of(TipoDeArchivo.JPG));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static UsuarioAutenticado usuario(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "X", TipoDeCuenta.PERSONAL, rol);
    }

    static final class BannersEnMemoria implements BannersRepositorio {
        private final Map<UUID, Banner> banners = new HashMap<>();
        private final Map<UUID, byte[]> imagenes = new HashMap<>();

        @Override
        public List<Banner> todos() {
            List<Banner> lista = new ArrayList<>(banners.values());
            lista.sort(Comparator.comparingInt(Banner::orden));
            return lista;
        }

        @Override
        public Optional<Banner> porId(UUID id) {
            return Optional.ofNullable(banners.get(id));
        }

        @Override
        public void guardar(Banner banner) {
            banners.put(banner.id(), banner);
        }

        @Override
        public void guardarImagen(UUID banner, byte[] jpeg) {
            imagenes.put(banner, jpeg);
        }

        @Override
        public Optional<byte[]> imagen(UUID banner) {
            return Optional.ofNullable(imagenes.get(banner));
        }

        @Override
        public void borrar(UUID banner) {
            banners.remove(banner);
            imagenes.remove(banner);
        }
    }
}
