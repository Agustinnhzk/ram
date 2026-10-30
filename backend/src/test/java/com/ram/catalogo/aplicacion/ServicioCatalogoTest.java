package com.ram.catalogo.aplicacion;

import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.MotivoDeCambio;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.SinPermisoException;
import com.ram.catalogo.TipoComponente;
import com.ram.catalogo.VistaPreviaDeAumento;
import com.ram.catalogo.dominio.Categoria;
import com.ram.catalogo.dominio.Pesos;
import com.ram.catalogo.dominio.Producto;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioCatalogoTest {

    private static final Categoria CAMARAS = new Categoria(UUID.randomUUID(), "camaras", "Cámaras", true);
    private static final Categoria NOTEBOOKS = new Categoria(UUID.randomUUID(), "notebooks", "Notebooks", false);
    private static final Categoria COMPONENTES = new Categoria(UUID.randomUUID(), "componentes", "Componentes", false);

    private final CatalogoEnMemoria memoria = new CatalogoEnMemoria();
    private final RelojDePrueba reloj = new RelojDePrueba();
    private UsuarioAutenticado logueado;
    private ServicioCatalogo servicio;

    private Producto kitCamaras;
    private Producto notebookReacondicionada;
    private Producto notebookSinPrecio;
    private Producto oculto;
    private Producto procesador;

    @BeforeEach
    void preparar() {
        memoria.agregar(CAMARAS);
        memoria.agregar(NOTEBOOKS);
        memoria.agregar(COMPONENTES);
        kitCamaras = producto("kit-4-camaras", "Kit 4 cámaras + DVR", CAMARAS, Condicion.NUEVO, 420_000L, true, null);
        notebookReacondicionada = producto("notebook-reac", "Notebook reacondicionada", NOTEBOOKS, Condicion.REACONDICIONADO, 450_000L, true, null);
        notebookSinPrecio = producto("notebook-gamer", "Notebook gamer", NOTEBOOKS, Condicion.NUEVO, null, true, null);
        oculto = producto("oculto", "Producto oculto", CAMARAS, Condicion.NUEVO, 100_000L, false, null);
        procesador = producto("ryzen", "AMD Ryzen 5 5600", COMPONENTES, Condicion.NUEVO, 165_000L, false,
                new DatosTecnicos(TipoComponente.CPU, "AM4", null, null, List.of(), 65, null, false));
        List.of(kitCamaras, notebookReacondicionada, notebookSinPrecio, oculto, procesador).forEach(memoria::agregar);

        logueado = usuario(Rol.DUENO);
        SesionActual sesion = () -> Optional.ofNullable(logueado);
        servicio = new ServicioCatalogo(memoria, memoria, memoria, sesion, reloj, java.time.ZoneId.of("America/Argentina/Buenos_Aires"));
    }

    // ─── Tienda ───────────────────────────────────────────────────────────────────────────────

    @Test
    void elBuscadorEncuentraSinTildesYConVariasPalabras() {
        assertThat(nombres(servicio.buscar(new FiltroCatalogo(null, null, "camaras dvr")))).containsExactly("Kit 4 cámaras + DVR");
        assertThat(servicio.buscar(new FiltroCatalogo(null, null, "camaras heladera"))).isEmpty();
    }

    @Test
    void laTiendaNoMuestraLosProductosNoPublicados() {
        assertThat(nombres(servicio.buscar(FiltroCatalogo.todo()))).doesNotContain("Producto oculto", "AMD Ryzen 5 5600");
        assertThat(servicio.obtenerPublicado("oculto")).isEmpty();
        assertThat(servicio.obtenerPublicado("kit-4-camaras")).isPresent();
    }

    @Test
    void filtraPorCategoriaYPorCondicion() {
        assertThat(nombres(servicio.buscar(new FiltroCatalogo("notebooks", Condicion.REACONDICIONADO, null))))
                .containsExactly("Notebook reacondicionada");
    }

    @Test
    void losComponentesSeVenAunqueNoEstenPublicadosComoProductos() {
        assertThat(servicio.componentes()).extracting(DatosProducto::slug).containsExactly("ryzen");
    }

    // ─── Precios ──────────────────────────────────────────────────────────────────────────────

    @Test
    void elDuenoCambiaUnPrecioYQuedaEnElHistorial() {
        servicio.cambiarPrecio(kitCamaras.id(), 450_000L);

        assertThat(kitCamaras.precio()).contains(new Pesos(450_000));
        List<DatosCambioDePrecio> historial = servicio.historialDePrecios(kitCamaras.id());
        assertThat(historial).singleElement().satisfies(c -> {
            assertThat(c.precioAnterior()).isEqualTo(420_000L);
            assertThat(c.precioNuevo()).isEqualTo(450_000L);
            assertThat(c.motivo()).isEqualTo(MotivoDeCambio.MANUAL);
            assertThat(c.usuario()).isEqualTo("Agustín");
            assertThat(c.cambiadoEn()).isEqualTo(reloj.instant());
        });
    }

    @Test
    void ponerElMismoPrecioNoEnsuciaElHistorial() {
        servicio.cambiarPrecio(kitCamaras.id(), 420_000L);

        assertThat(memoria.cambios()).isEmpty();
    }

    @Test
    void unProductoPuedePasarAPrecioAConsultar() {
        servicio.cambiarPrecio(kitCamaras.id(), null);

        assertThat(kitCamaras.precio()).isEmpty();
        assertThat(memoria.cambios()).singleElement().satisfies(c -> assertThat(c.nuevo()).isNull());
    }

    @Test
    void soloElDuenoCambiaPrecios() {
        logueado = usuario(Rol.TECNICO);

        assertThatThrownBy(() -> servicio.cambiarPrecio(kitCamaras.id(), 1L)).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.aplicarAumento(BigDecimal.TEN, null)).isInstanceOf(SinPermisoException.class);
        assertThat(kitCamaras.precio()).contains(new Pesos(420_000));
    }

    @Test
    void noSePuedeCambiarElPrecioDeUnProductoQueNoExiste() {
        assertThatThrownBy(() -> servicio.cambiarPrecio(ProductoId.nuevo(), 1L)).isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    void laVistaPreviaMuestraLosPreciosNuevosSinCambiarNada() {
        VistaPreviaDeAumento vista = servicio.simularAumento(new BigDecimal("10"), "notebooks");

        // La notebook sin precio no está: no hay nada que aumentar.
        assertThat(vista.precios()).singleElement().satisfies(p -> {
            assertThat(p.nombre()).isEqualTo("Notebook reacondicionada");
            assertThat(p.precioActual()).isEqualTo(450_000L);
            assertThat(p.precioNuevo()).isEqualTo(495_000L);
        });
        assertThat(notebookReacondicionada.precio()).contains(new Pesos(450_000));
        assertThat(memoria.cambios()).isEmpty();
    }

    @Test
    void elAumentoATodoTocaSoloLosQueTienenPrecioYQuedaEnElHistorial() {
        int cambiados = servicio.aplicarAumento(new BigDecimal("10"), null);

        assertThat(cambiados).isEqualTo(4); // kit, notebook reacondicionada, oculto y procesador
        assertThat(kitCamaras.precio()).contains(new Pesos(462_000));
        assertThat(notebookSinPrecio.precio()).isEmpty();
        assertThat(memoria.cambios()).hasSize(4).allSatisfy(c -> {
            assertThat(c.motivo()).isEqualTo(MotivoDeCambio.PORCENTAJE);
            assertThat(c.porcentaje()).isEqualByComparingTo("10");
        });
    }

    @Test
    void elAumentoPorCategoriaNoTocaLasDemas() {
        servicio.aplicarAumento(new BigDecimal("10"), "notebooks");

        assertThat(notebookReacondicionada.precio()).contains(new Pesos(495_000));
        assertThat(kitCamaras.precio()).contains(new Pesos(420_000));
    }

    @Test
    void unaCategoriaQueNoExisteEsUnError() {
        assertThatThrownBy(() -> servicio.simularAumento(BigDecimal.TEN, "heladeras"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elHistorialVaDelCambioMasNuevoAlMasViejo() {
        servicio.cambiarPrecio(kitCamaras.id(), 430_000L);
        reloj.avanzar(Duration.ofDays(30));
        servicio.cambiarPrecio(kitCamaras.id(), 440_000L);

        assertThat(servicio.historialDePrecios(kitCamaras.id()))
                .extracting(DatosCambioDePrecio::precioNuevo)
                .containsExactly(440_000L, 430_000L);
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private static Producto producto(String slug, String nombre, Categoria categoria, Condicion condicion, Long precio,
                                     boolean publicado, DatosTecnicos tecnicos) {
        return new Producto(ProductoId.nuevo(), slug, nombre, categoria, condicion, precio == null ? null : new Pesos(precio),
                null, "", "", List.of(), null, publicado, tecnicos);
    }

    private static UsuarioAutenticado usuario(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín", TipoDeCuenta.PERSONAL, rol);
    }

    private static List<String> nombres(List<DatosProducto> productos) {
        return productos.stream().map(DatosProducto::nombre).toList();
    }

    /** Un reloj que no avanza solo: los tests deciden qué hora es. */
    private static final class RelojDePrueba extends Clock {

        private Instant ahora = Instant.parse("2026-10-29T15:00:00Z");

        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
