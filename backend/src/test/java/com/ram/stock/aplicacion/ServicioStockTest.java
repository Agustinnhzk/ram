package com.ram.stock.aplicacion;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.stock.DatosReserva;
import com.ram.stock.DisponibilidadPublica;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.EstadoDeStock;
import com.ram.stock.FilaDeStock;
import com.ram.stock.OperacionDeStockInvalidaException;
import com.ram.stock.SinPermisoException;
import com.ram.stock.TipoDeMovimiento;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioStockTest {

    private static final UUID TALLER = UUID.randomUUID();
    private static final DatosCategoria CAMARAS = new DatosCategoria("camaras", "Cámaras", true);
    private static final DatosProducto KIT = producto("kit-4-camaras", "Kit 4 cámaras", true);
    private static final DatosProducto DVR = producto("dvr", "DVR 8 canales", true);
    private static final DatosProducto OCULTO = producto("viejo", "Producto oculto", false);

    private final StockEnMemoria repo = new StockEnMemoria();
    private UsuarioAutenticado logueado = personal(Rol.MOSTRADOR);
    private ServicioStock servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioStock(repo, catalogo(List.of(KIT, DVR, OCULTO)), () -> Optional.ofNullable(logueado),
                Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void sinControlNoAparecenEnLaTienda_conControlSeVeElEstado() {
        assertThat(servicio.paraLaTienda()).isEmpty();

        servicio.controlar(KIT.id(), 10);
        servicio.controlar(DVR.id(), 2);
        servicio.controlar(OCULTO.id(), 0);

        List<DisponibilidadPublica> tienda = servicio.paraLaTienda();
        assertThat(tienda).hasSize(2); // el oculto no se publica
        assertThat(tienda.get(0)).isEqualTo(new DisponibilidadPublica("kit-4-camaras", EstadoDeStock.DISPONIBLE, null));
        assertThat(tienda.get(1)).isEqualTo(new DisponibilidadPublica("dvr", EstadoDeStock.ULTIMAS_UNIDADES, 2));
    }

    @Test
    void cadaCambioQuedaEnElHistorial() {
        servicio.controlar(KIT.id(), 3);
        servicio.ingresar(KIT.id(), 5, "Factura 0001-123");
        servicio.ajustar(KIT.id(), 7, "Una vino rota");

        assertThat(repo.existencias.get(KIT.id())).isEqualTo(7);
        assertThat(servicio.movimientos(KIT.id()))
                .extracting(m -> m.tipo() + " " + m.cantidad() + " → " + m.saldo())
                .containsExactly("AJUSTE -1 → 7", "INGRESO 5 → 8", "INICIAL 3 → 3");
        assertThat(servicio.movimientos(KIT.id()).getFirst().usuario()).isEqualTo("Personal");
        assertThatThrownBy(() -> servicio.ajustar(KIT.id(), 6, "  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noSeControlaDosVeces_niSeCargaSinControl() {
        servicio.controlar(KIT.id(), 1);

        assertThatThrownBy(() -> servicio.controlar(KIT.id(), 5)).isInstanceOf(OperacionDeStockInvalidaException.class);
        assertThatThrownBy(() -> servicio.ingresar(DVR.id(), 5, null)).isInstanceOf(OperacionDeStockInvalidaException.class);
        assertThatThrownBy(() -> servicio.controlar(ProductoId.nuevo(), 5)).isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    void alConfirmarElPagoSeDescuentaLoQueHay_yLoQueFaltaQuedaReservado() {
        servicio.controlar(KIT.id(), 1);
        UUID pedido = UUID.randomUUID();

        // El DVR no tiene control de stock: no se toca.
        servicio.descontarPorVenta(pedido, 12, Map.of(KIT.id(), 3, DVR.id(), 1));

        assertThat(repo.existencias.get(KIT.id())).isZero();
        assertThat(repo.existencias).doesNotContainKey(DVR.id());
        List<DatosReserva> reservas = servicio.reservasDePedido(pedido);
        assertThat(reservas).hasSize(1);
        assertThat(reservas.getFirst().cantidad()).isEqualTo(2);
        assertThat(reservas.getFirst().numeroPedido()).isEqualTo(12);
        assertThat(reservas.getFirst().sePuedeCubrir()).isFalse();
    }

    @Test
    void noDescuentaDosVecesElMismoPedido() {
        servicio.controlar(KIT.id(), 5);
        UUID pedido = UUID.randomUUID();

        servicio.descontarPorVenta(pedido, 1, Map.of(KIT.id(), 2));
        servicio.descontarPorVenta(pedido, 1, Map.of(KIT.id(), 2));

        assertThat(repo.existencias.get(KIT.id())).isEqualTo(3);
    }

    @Test
    void laReservaSeCubreAManoCuandoLlegaMercaderia() {
        servicio.controlar(KIT.id(), 0);
        UUID pedido = UUID.randomUUID();
        servicio.descontarPorVenta(pedido, 7, Map.of(KIT.id(), 2));
        UUID reserva = servicio.reservasPendientes().getFirst().id();

        servicio.ingresar(KIT.id(), 1, null);
        assertThatThrownBy(() -> servicio.cubrirReserva(reserva))
                .isInstanceOf(OperacionDeStockInvalidaException.class)
                .hasMessageContaining("falta 1 unidad");

        servicio.ingresar(KIT.id(), 3, null);
        assertThat(servicio.reservasPendientes().getFirst().sePuedeCubrir()).isTrue();
        servicio.cubrirReserva(reserva);

        assertThat(repo.existencias.get(KIT.id())).isEqualTo(2);
        assertThat(servicio.reservasPendientes()).isEmpty();
        assertThat(servicio.reservasDePedido(pedido).getFirst().estado()).isEqualTo(EstadoDeReserva.CUBIERTA);
        assertThat(servicio.movimientos(KIT.id()).getFirst().tipo()).isEqualTo(TipoDeMovimiento.RESERVA_CUBIERTA);
        assertThat(servicio.movimientos(KIT.id()).getFirst().numeroPedido()).isEqualTo(7);
    }

    @Test
    void cancelarUnPedidoPagadoDevuelveTodo_loVendidoYLoCubierto_yCancelaLoPendiente() {
        servicio.controlar(KIT.id(), 1);
        servicio.controlar(DVR.id(), 0);
        UUID pedido = UUID.randomUUID();
        servicio.descontarPorVenta(pedido, 3, Map.of(KIT.id(), 2, DVR.id(), 1));
        // Kit: 1 vendido + 1 reservado. Llega 1 kit y se cubre. El DVR sigue reservado.
        servicio.ingresar(KIT.id(), 1, null);
        UUID reservaKit = servicio.reservasDePedido(pedido).stream().filter(r -> r.producto().equals(KIT.id())).findFirst().orElseThrow().id();
        servicio.cubrirReserva(reservaKit);
        assertThat(repo.existencias.get(KIT.id())).isZero();

        servicio.devolverPorCancelacion(pedido, 3);
        servicio.devolverPorCancelacion(pedido, 3); // dos veces: no devuelve el doble

        assertThat(repo.existencias.get(KIT.id())).isEqualTo(2);
        assertThat(repo.existencias.get(DVR.id())).isZero();
        assertThat(servicio.reservasPendientes()).isEmpty();
        assertThat(servicio.reservasDePedido(pedido)).extracting(DatosReserva::estado)
                .contains(EstadoDeReserva.CANCELADA, EstadoDeReserva.CUBIERTA);
    }

    @Test
    void elListadoMuestraTodoElCatalogoConSuStockYReservas() {
        servicio.controlar(KIT.id(), 0);
        servicio.descontarPorVenta(UUID.randomUUID(), 1, Map.of(KIT.id(), 2));

        List<FilaDeStock> filas = servicio.listar();
        assertThat(filas).hasSize(3);
        FilaDeStock kit = filas.getFirst();
        assertThat(kit.cantidad()).isZero();
        assertThat(kit.reservado()).isEqualTo(2);
        assertThat(filas.get(1).controlado()).isFalse();
    }

    @Test
    void dejarDeControlar_soloElDueno_ySinReservas() {
        servicio.controlar(KIT.id(), 0);
        servicio.descontarPorVenta(UUID.randomUUID(), 1, Map.of(KIT.id(), 1));

        assertThatThrownBy(() -> servicio.dejarDeControlar(KIT.id())).isInstanceOf(SinPermisoException.class);
        logueado = personal(Rol.DUENO);
        assertThatThrownBy(() -> servicio.dejarDeControlar(KIT.id())).isInstanceOf(OperacionDeStockInvalidaException.class);

        servicio.controlar(DVR.id(), 4);
        servicio.dejarDeControlar(DVR.id());
        assertThat(repo.existencias).doesNotContainKey(DVR.id());
        assertThat(servicio.movimientos(DVR.id())).hasSize(1); // la historia queda
    }

    @Test
    void elTecnicoMiraPeroNoCarga_yUnClienteNiMira() {
        logueado = personal(Rol.TECNICO);
        assertThat(servicio.listar()).hasSize(3);
        assertThatThrownBy(() -> servicio.controlar(KIT.id(), 1)).isInstanceOf(SinPermisoException.class);

        logueado = new UsuarioAutenticado(UUID.randomUUID(), TALLER, "Cliente", TipoDeCuenta.CLIENTE, null);
        assertThatThrownBy(() -> servicio.listar()).isInstanceOf(SinPermisoException.class);
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private static DatosProducto producto(String slug, String nombre, boolean publicado) {
        return new DatosProducto(ProductoId.nuevo(), slug, nombre, CAMARAS, Condicion.NUEVO, 100_000L, null, "", "",
                List.of(), null, publicado, null);
    }

    /** Un catálogo con solo lo que usa stock: los productos y buscar uno por id. El resto no se llama. */
    private static Catalogo catalogo(List<DatosProducto> productos) {
        return (Catalogo) Proxy.newProxyInstance(Catalogo.class.getClassLoader(), new Class<?>[]{Catalogo.class},
                (proxy, metodo, args) -> switch (metodo.getName()) {
                    case "listarParaPrecios" -> productos;
                    case "obtener" -> productos.stream().filter(p -> p.id().equals(args[0])).findFirst();
                    default -> throw new UnsupportedOperationException(metodo.getName());
                });
    }

    private static UsuarioAutenticado personal(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), TALLER, "Personal", TipoDeCuenta.PERSONAL, rol);
    }
}
