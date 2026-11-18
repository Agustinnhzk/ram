package com.ram.trabajos.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.ImagenLista;
import com.ram.archivos.TipoDeArchivo;
import com.ram.trabajos.DatosTrabajo;
import com.ram.trabajos.SinPermisoException;
import com.ram.trabajos.TipoDeTrabajo;
import com.ram.trabajos.TrabajoNuevo;
import com.ram.trabajos.dominio.Trabajo;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioTrabajosTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 17);

    private final TrabajosEnMemoria memoria = new TrabajosEnMemoria();
    private UsuarioAutenticado logueado = usuario(Rol.DUENO);
    private ServicioTrabajos servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioTrabajos(memoria, () -> Optional.ofNullable(logueado),
                Clock.fixed(Instant.parse("2026-11-17T15:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Argentina/Buenos_Aires"));
    }

    @Test
    void elSlugSaleDelTituloYElBarrio_yNoCambiaAlEditar() {
        UUID id = servicio.crear(datos("Notebook que no encendía", "B° Abasto", true));
        assertThat(servicio.obtener(id).orElseThrow().slug()).isEqualTo("notebook-que-no-encendia-abasto");

        UUID otro = servicio.crear(datos("Notebook que no encendía", "Abasto", true));
        assertThat(servicio.obtener(otro).orElseThrow().slug()).isEqualTo("notebook-que-no-encendia-abasto-2");

        servicio.editar(id, datos("Notebook Dell que no encendía", "Abasto", true));
        assertThat(servicio.obtener(id).orElseThrow().slug()).isEqualTo("notebook-que-no-encendia-abasto");
        assertThat(servicio.obtener(id).orElseThrow().titulo()).isEqualTo("Notebook Dell que no encendía");
    }

    @Test
    void laTiendaVeSoloLosPublicados_delMasRecienteAlMasViejo() {
        UUID viejo = servicio.crear(conFecha(datos("PC que se reiniciaba", "Centro", true), HOY.minusDays(20)));
        servicio.crear(datos("Borrador sin publicar", "Centro", false));
        UUID nuevo = servicio.crear(conFecha(datos("Cuatro cámaras en una casa", "Funes", true), HOY.minusDays(2)));

        assertThat(servicio.publicados().stream().map(DatosTrabajo::id).toList()).containsExactly(nuevo, viejo);
        assertThat(servicio.todos()).hasSize(3);
        assertThat(servicio.publicado("borrador-sin-publicar-centro")).isEmpty();
        assertThat(servicio.publicado("pc-que-se-reiniciaba-centro")).isPresent();
    }

    @Test
    void sinPermisoDelClienteNoSePuedePublicar() {
        TrabajoNuevo sinPermiso = new TrabajoNuevo(TipoDeTrabajo.CAMARAS, "Cámaras en un local", "Centro", true,
                "Querían ver el local desde el celular", null, "Instalamos 4 cámaras", null, HOY, null, false, true);
        assertThatThrownBy(() -> servicio.crear(sinPermiso)).hasMessageContaining("permiso");
    }

    @Test
    void validaLosDatos() {
        assertThatThrownBy(() -> servicio.crear(new TrabajoNuevo(null, "Notebook rota", "Centro", false, "No anda", null, "Se arregló", null,
                HOY, null, true, true))).hasMessageContaining("tipo");
        assertThatThrownBy(() -> servicio.crear(datos("PC", "Centro", true))).hasMessageContaining("título");
        assertThatThrownBy(() -> servicio.crear(datos("Notebook que no encendía", "Córdoba 1452", true)))
                .hasMessageContaining("sin calle ni altura");
        assertThatThrownBy(() -> servicio.crear(conFecha(datos("Notebook que no encendía", "Centro", true), HOY.plusDays(1))))
                .hasMessageContaining("futura");
    }

    @Test
    void sugiereLosBarriosYaUsados_unoPorZona() {
        servicio.crear(conFecha(datos("Notebook que no encendía", "alberdi", true), HOY.minusDays(5)));
        servicio.crear(conFecha(datos("PC que se reiniciaba", "B° Alberdi", true), HOY.minusDays(1)));
        servicio.crear(datos("Alarma en una casa", "Funes", true));

        assertThat(servicio.barrios()).containsExactly("Alberdi", "Funes");
    }

    @Test
    void lasFotosDeUnTrabajoSinPublicarNoSeVenDesdeLaTienda() {
        UUID publicado = servicio.crear(datos("Notebook que no encendía", "Abasto", true));
        UUID borrador = servicio.crear(datos("PC que se reiniciaba", "Abasto", false));
        UUID fotoPublica = servicio.agregarFoto(publicado, jpg());
        UUID fotoPrivada = servicio.agregarFoto(borrador, jpg());

        assertThat(servicio.fotoPublica(fotoPublica, true)).isPresent();
        assertThat(servicio.fotoPublica(fotoPrivada, true)).isEmpty();
        assertThat(servicio.foto(fotoPrivada, true)).isPresent();
    }

    @Test
    void fotos_ordenarBorrarYMaximo() {
        UUID id = servicio.crear(datos("Notebook que no encendía", "Abasto", true));
        UUID a = servicio.agregarFoto(id, jpg());
        UUID b = servicio.agregarFoto(id, jpg());

        servicio.ordenarFotos(id, List.of(b, a));
        assertThat(servicio.obtener(id).orElseThrow().fotos()).containsExactly(b, a);
        assertThatThrownBy(() -> servicio.ordenarFotos(id, List.of(b))).isInstanceOf(IllegalArgumentException.class);

        servicio.borrarFoto(id, b);
        assertThat(servicio.obtener(id).orElseThrow().fotos()).containsExactly(a);

        for (int i = 1; i < ServicioTrabajos.FOTOS_MAXIMAS; i++) {
            servicio.agregarFoto(id, jpg());
        }
        assertThatThrownBy(() -> servicio.agregarFoto(id, jpg())).hasMessageContaining("hasta " + ServicioTrabajos.FOTOS_MAXIMAS);
    }

    @Test
    void borrarSeLlevaLasFotos() {
        UUID id = servicio.crear(datos("Notebook que no encendía", "Abasto", true));
        UUID foto = servicio.agregarFoto(id, jpg());

        servicio.borrar(id);

        assertThat(servicio.todos()).isEmpty();
        assertThat(servicio.foto(foto, false)).isEmpty();
    }

    @Test
    void elVideoSeGuardaLimpio_ySePuedeSacar() {
        TrabajoNuevo d = datos("Notebook que no encendía", "Abasto", true);
        UUID id = servicio.crear(conVideo(d, "https://www.instagram.com/reel/DCxYz12AbC/?igsh=MWQ1ZGUxMzBkMA=="));
        assertThat(servicio.obtener(id).orElseThrow().video()).isEqualTo("https://www.instagram.com/reel/DCxYz12AbC/");

        servicio.editar(id, conVideo(d, "  "));
        assertThat(servicio.obtener(id).orElseThrow().video()).isNull();

        assertThatThrownBy(() -> servicio.editar(id, conVideo(d, "https://www.tiktok.com/@ram/video/123")))
                .hasMessageContaining("Instagram");
    }

    @Test
    void soloElDuenoCargaYPublica() {
        logueado = usuario(Rol.MOSTRADOR);
        assertThatThrownBy(() -> servicio.crear(datos("Notebook que no encendía", "Abasto", true))).isInstanceOf(SinPermisoException.class);
    }

    private static TrabajoNuevo datos(String titulo, String barrio, boolean publicado) {
        return new TrabajoNuevo(TipoDeTrabajo.NOTEBOOK, titulo, barrio, true, "No encendía después de una tormenta",
                "Falla en el circuito de carga", "Reparación de placa y prueba de 24 h", null, HOY, 901, true, publicado);
    }

    private static TrabajoNuevo conFecha(TrabajoNuevo d, LocalDate fecha) {
        return new TrabajoNuevo(d.tipo(), d.titulo(), d.barrio(), d.aDomicilio(), d.problema(), d.diagnostico(), d.solucion(),
                d.video(), fecha, d.numeroDeOrden(), d.consentimiento(), d.publicado());
    }

    private static TrabajoNuevo conVideo(TrabajoNuevo d, String video) {
        return new TrabajoNuevo(d.tipo(), d.titulo(), d.barrio(), d.aDomicilio(), d.problema(), d.diagnostico(), d.solucion(),
                video, d.fecha(), d.numeroDeOrden(), d.consentimiento(), d.publicado());
    }

    private static ArchivoSubido jpg() {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB), "jpeg", salida);
            return ArchivoSubido.revisar("t.jpg", salida.toByteArray(), Set.of(TipoDeArchivo.JPG));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static UsuarioAutenticado usuario(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "X", TipoDeCuenta.PERSONAL, rol);
    }

    /** Como la base: ordena por fecha (y carga) del más nuevo al más viejo. */
    static final class TrabajosEnMemoria implements TrabajosRepositorio {
        private final Map<UUID, Trabajo> trabajos = new HashMap<>();
        private final Map<UUID, List<UUID>> fotos = new LinkedHashMap<>();
        private final Map<UUID, byte[]> archivos = new HashMap<>();
        private long carga;
        private final Map<UUID, Long> ordenDeCarga = new HashMap<>();

        @Override
        public List<Trabajo> todos() {
            List<Trabajo> lista = new ArrayList<>(trabajos.values());
            lista.sort(Comparator.comparing(Trabajo::fecha).thenComparing(t -> ordenDeCarga.get(t.id())).reversed());
            return lista;
        }

        @Override
        public Optional<Trabajo> porId(UUID id) {
            return Optional.ofNullable(trabajos.get(id));
        }

        @Override
        public Optional<Trabajo> porSlug(String slug) {
            return trabajos.values().stream().filter(t -> t.slug().equals(slug)).findFirst();
        }

        @Override
        public boolean existeSlug(String slug) {
            return porSlug(slug).isPresent();
        }

        @Override
        public void guardar(Trabajo trabajo) {
            ordenDeCarga.putIfAbsent(trabajo.id(), carga++);
            trabajos.put(trabajo.id(), trabajo);
        }

        @Override
        public void borrar(UUID id) {
            fotosDe(id).forEach(archivos::remove);
            fotos.remove(id);
            trabajos.remove(id);
        }

        @Override
        public List<UUID> fotosDe(UUID trabajo) {
            return List.copyOf(fotos.getOrDefault(trabajo, List.of()));
        }

        @Override
        public Map<UUID, List<UUID>> fotosDeTodos() {
            return fotos;
        }

        @Override
        public Optional<UUID> trabajoDeLaFoto(UUID foto) {
            return fotos.entrySet().stream().filter(e -> e.getValue().contains(foto)).map(Map.Entry::getKey).findFirst();
        }

        @Override
        public UUID agregarFoto(UUID trabajo, ImagenLista grande, ImagenLista chica) {
            UUID id = UUID.randomUUID();
            fotos.computeIfAbsent(trabajo, k -> new ArrayList<>()).add(id);
            archivos.put(id, chica.jpeg());
            return id;
        }

        @Override
        public void borrarFoto(UUID foto) {
            fotos.values().forEach(l -> l.remove(foto));
            archivos.remove(foto);
        }

        @Override
        public void ordenarFotos(UUID trabajo, List<UUID> orden) {
            fotos.put(trabajo, new ArrayList<>(orden));
        }

        @Override
        public Optional<byte[]> archivo(UUID foto, boolean chica) {
            return Optional.ofNullable(archivos.get(foto));
        }
    }
}
