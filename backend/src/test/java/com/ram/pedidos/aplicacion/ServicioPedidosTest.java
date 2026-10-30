package com.ram.pedidos.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.catalogo.TipoComponente;
import com.ram.pedidos.Cotizacion;
import com.ram.pedidos.DetalleDePedido;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.FormaDeEntrega;
import com.ram.pedidos.ItemPedido;
import com.ram.pedidos.NuevoPedido;
import com.ram.pedidos.OperacionNoPermitidaException;
import com.ram.pedidos.PagoInformado;
import com.ram.pedidos.PedidoNoEncontradoException;
import com.ram.pedidos.PedidoRealizado;
import com.ram.pedidos.ProductoNoDisponibleException;
import com.ram.pedidos.SinPermisoException;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ram.pedidos.Productos.COMPONENTES_TODOS;
import static com.ram.pedidos.Productos.KIT;
import static com.ram.pedidos.Productos.RYZEN_5600;
import static com.ram.pedidos.Productos.SIN_PRECIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioPedidosTest {

    private static final UUID TALLER = UUID.randomUUID();

    private final DoblesDePedidos.PedidosEnMemoria pedidos = new DoblesDePedidos.PedidosEnMemoria();
    private final DoblesDePedidos.StockFalso stock = new DoblesDePedidos.StockFalso();
    private final DoblesDePedidos.ClientesFalsos clientes = new DoblesDePedidos.ClientesFalsos();
    private final List<Object> eventos = new ArrayList<>();
    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC);
    private final UsuarioAutenticado ana = cliente("Ana López");
    private UsuarioAutenticado logueado;
    private ServicioPedidos servicio;

    @BeforeEach
    void preparar() {
        logueado = ana;
        servicio = new ServicioPedidos(pedidos, new DoblesDePedidos.CatalogoFalso(List.of(KIT, SIN_PRECIO, com.ram.pedidos.Productos.EN_OFERTA), COMPONENTES_TODOS),
                stock, clientes, () -> Optional.ofNullable(logueado), eventos::add, reloj);
    }

    // ─── Crear ────────────────────────────────────────────────────────────────────────────────

    @Test
    void losPreciosSalenDelCatalogo_noDelCarrito() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 2), pcAm4(1)));

        assertThat(pedido.numero()).isEqualTo(1);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ESPERANDO_PAGO);
        assertThat(pedido.lineas().get(0).precioUnitario()).isEqualTo(250_000L);
        assertThat(pedido.lineas().get(0).subtotal()).isEqualTo(500_000L);
        assertThat(pedido.lineas().get(1).descripcion()).isEqualTo("PC armada a medida");
        assertThat(pedido.lineas().get(1).precioUnitario()).isEqualTo(860_000L);
        assertThat(pedido.lineas().get(2).descripcion()).isEqualTo("Armado y prueba de la PC");
        assertThat(pedido.lineas().get(2).precioUnitario()).isEqualTo(30_000L);
        assertThat(pedido.total()).isEqualTo(1_390_000L);
        assertThat(pedido.cliente()).isEqualTo("Ana López");
        assertThat(pedido.datosDePago().alias()).isEqualTo("ram.nh");
        assertThat(pedido.puedeSubirComprobante()).isTrue();
    }

    @Test
    void sinArmadoSeCompranSoloLosComponentes() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.PcArmada(piezasAm4(), false, false, 2)));

        assertThat(pedido.lineas()).hasSize(1);
        assertThat(pedido.lineas().getFirst().descripcion()).isEqualTo("Componentes para armar tu PC");
        assertThat(pedido.lineas().getFirst().referencia()).contains("&armado=no");
        assertThat(pedido.total()).isEqualTo(1_720_000L);
    }

    @Test
    void elArmadoSeCobraPorCadaPc() {
        DetalleDePedido pedido = servicio.realizar(pedido(pcAm4(2)));

        assertThat(pedido.lineas().get(1).cantidad()).isEqualTo(2);
        assertThat(pedido.total()).isEqualTo(2 * (860_000L + 30_000L));
    }

    @Test
    void siElTallerNoOfreceElArmadoSoloSePuedenComprarLosComponentes() {
        pedidos.precioDelArmado = null;

        assertThatThrownBy(() -> servicio.realizar(pedido(pcAm4(1))))
                .isInstanceOf(ProductoNoDisponibleException.class)
                .hasMessageContaining("armado");
        assertThat(servicio.realizar(pedido(new ItemPedido.PcArmada(piezasAm4(), false, false, 1))).total()).isEqualTo(860_000L);
    }

    @Test
    void laCotizacionCalculaSinGuardar_yAvisaSiElEnvioEsACotizar() {
        Cotizacion cotizacion = servicio.cotizar(new NuevoPedido(List.of(new ItemPedido.Producto("kit-4-camaras", 2)),
                FormaDeEntrega.ENVIO_OTRA_LOCALIDAD, "San Martín 100, Funes", "3415551234", null));

        assertThat(cotizacion.total()).isEqualTo(500_000L);
        assertThat(cotizacion.envioACotizar()).isTrue();
        assertThat(pedidos.pedidos.values()).isEmpty();
        assertThat(eventos).isEmpty();

        Cotizacion enRosario = servicio.cotizar(new NuevoPedido(List.of(new ItemPedido.Producto("kit-4-camaras", 1)),
                FormaDeEntrega.ENVIO_ROSARIO, "Córdoba 1234", "3415551234", null));
        assertThat(enRosario.envioACotizar()).isFalse();
    }

    @Test
    void unProductoEnOfertaSeCobraAlPrecioDeOferta() {
        assertThat(servicio.realizar(pedido(new ItemPedido.Producto("dvr-oferta", 2))).total()).isEqualTo(500_000L);
    }

    @Test
    void soloElDuenoCambiaElPrecioDelArmado() {
        logueado = personal(Rol.MOSTRADOR);
        assertThatThrownBy(() -> servicio.cambiarPrecioDelArmado(35_000L)).isInstanceOf(SinPermisoException.class);

        logueado = personal(Rol.DUENO);
        servicio.cambiarPrecioDelArmado(35_000L);
        assertThat(servicio.precioDelArmado()).contains(35_000L);
        assertThatThrownBy(() -> servicio.cambiarPrecioDelArmado(0L)).isInstanceOf(IllegalArgumentException.class);
        servicio.cambiarPrecioDelArmado(null);
        assertThat(servicio.precioDelArmado()).isEmpty();
    }

    @Test
    void avisaQueEntroUnPedido() {
        servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        assertThat(eventos).singleElement().satisfies(e -> {
            PedidoRealizado evento = (PedidoRealizado) e;
            assertThat(evento.numero()).isEqualTo(1);
            assertThat(evento.total()).isEqualTo(250_000L);
            assertThat(evento.lineas()).containsExactly("1 × Kit 4 cámaras + DVR — $ 250.000");
        });
    }

    @Test
    void losNumerosSonCorrelativos() {
        servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        DetalleDePedido segundo = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        assertThat(segundo.numero()).isEqualTo(2);
    }

    @Test
    void unProductoQueNoExisteOSinPrecioNoSePuedeComprar() {
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.Producto("no-existe", 1))))
                .isInstanceOf(ProductoNoDisponibleException.class);
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.Producto("alarma-x", 1))))
                .isInstanceOf(ProductoNoDisponibleException.class)
                .hasMessageContaining("Alarma X no tiene precio");
        assertThat(pedidos.pedidos.values()).isEmpty();
        assertThat(eventos).isEmpty();
    }

    @Test
    void unaPcIncompatibleOConPiezasSinPrecioSeRechaza() {
        Map<TipoComponente, String> incompatible = piezasAm4();
        incompatible.put(TipoComponente.CPU, "ryzen-5-8600g");
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.PcArmada(incompatible, false, true, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("socket");

        Map<TipoComponente, String> sinPrecio = piezasAm4();
        sinPrecio.put(TipoComponente.GABINETE, "gabinete-atx");
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.PcArmada(sinPrecio, false, true, 1))))
                .isInstanceOf(ProductoNoDisponibleException.class)
                .hasMessageContaining("Gabinete ATX");

        Map<TipoComponente, String> inventada = piezasAm4();
        inventada.put(TipoComponente.RAM, "ram-inventada");
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.PcArmada(inventada, false, true, 1))))
                .isInstanceOf(ProductoNoDisponibleException.class);
    }

    @Test
    void validaElCheckout() {
        assertThatThrownBy(() -> servicio.realizar(new NuevoPedido(List.of(), FormaDeEntrega.RETIRO, null, "3415551234", null)))
                .hasMessageContaining("vacío");
        assertThatThrownBy(() -> servicio.realizar(new NuevoPedido(List.of(new ItemPedido.Producto("kit-4-camaras", 1)),
                FormaDeEntrega.ENVIO_ROSARIO, null, "3415551234", null))).hasMessageContaining("dirección");
        assertThatThrownBy(() -> servicio.realizar(new NuevoPedido(List.of(new ItemPedido.Producto("kit-4-camaras", 1)),
                FormaDeEntrega.RETIRO, null, "12", null))).hasMessageContaining("teléfono");
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 50))))
                .hasMessageContaining("cantidad");
    }

    @Test
    void unClienteNoPuedeAcumularPedidosSinPagar() {
        for (int i = 0; i < ServicioPedidos.PEDIDOS_SIN_PAGAR_MAXIMOS; i++) {
            servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        }
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1))))
                .isInstanceOf(OperacionNoPermitidaException.class);

        logueado = cliente("Otro");
        assertThat(servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1))).numero()).isEqualTo(6);
    }

    @Test
    void elPersonalNoCompraDesdeLaTienda() {
        logueado = personal(Rol.DUENO);
        assertThatThrownBy(() -> servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1))))
                .isInstanceOf(SinPermisoException.class);
    }

    // ─── Comprobante y privacidad ─────────────────────────────────────────────────────────────

    @Test
    void elClienteSubeElComprobante_yElTallerLoVe() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        servicio.informarPago(pedido.id(), comprobante());

        assertThat(eventos.getLast()).isEqualTo(new PagoInformado(1, "Ana López", "341 555-1234", 250_000L));
        DetalleDePedido visto = servicio.miPedido(pedido.id()).orElseThrow();
        assertThat(visto.estado()).isEqualTo(EstadoPedido.PAGO_INFORMADO);
        assertThat(visto.puedeSubirComprobante()).isFalse();
        assertThat(visto.puedeCancelar()).isFalse();

        logueado = personal(Rol.MOSTRADOR);
        UUID comprobante = servicio.obtener(pedido.id()).orElseThrow().comprobantes().getFirst().id();
        var archivo = servicio.comprobante(pedido.id(), comprobante).orElseThrow();
        assertThat(archivo.tipoDeContenido()).isEqualTo("image/png");
        assertThat(archivo.nombre()).isEqualTo("transferencia.png");
        assertThat(servicio.comprobante(pedido.id(), UUID.randomUUID())).isEmpty();
    }

    @Test
    void unClienteNoVeNiTocaPedidosDeOtro() {
        DetalleDePedido deAna = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        logueado = cliente("Intruso");
        assertThat(servicio.miPedido(deAna.id())).isEmpty();
        assertThat(servicio.misPedidos()).isEmpty();
        assertThatThrownBy(() -> servicio.informarPago(deAna.id(), comprobante())).isInstanceOf(PedidoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.cancelarMiPedido(deAna.id())).isInstanceOf(PedidoNoEncontradoException.class);
    }

    @Test
    void unClienteNoUsaLasAccionesDelSistema() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        assertThatThrownBy(() -> servicio.confirmarPago(pedido.id())).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.listar(null)).isInstanceOf(SinPermisoException.class);
    }

    @Test
    void elComprobanteTieneQueSerImagenOPdf() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        ArchivoSubido word = new ArchivoSubido("pago.docx", TipoDeArchivo.DOCX, new byte[]{'P', 'K', 3, 4});

        assertThatThrownBy(() -> servicio.informarPago(pedido.id(), word)).isInstanceOf(IllegalArgumentException.class);
    }

    // ─── Sistema ──────────────────────────────────────────────────────────────────────────────

    @Test
    void elTallerConfirmaYEntrega_yElListadoMuestraLosActivos() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        servicio.informarPago(pedido.id(), comprobante());

        logueado = personal(Rol.MOSTRADOR);
        assertThat(servicio.listar(null)).hasSize(1);
        assertThat(servicio.listar(EstadoPedido.PAGO_INFORMADO)).hasSize(1);
        servicio.confirmarPago(pedido.id());

        logueado = personal(Rol.TECNICO);
        servicio.entregar(pedido.id());

        assertThat(servicio.listar(null)).isEmpty();
        assertThat(servicio.listar(EstadoPedido.ENTREGADO)).hasSize(1);
    }

    @Test
    void elTecnicoNoManejaPagos() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        logueado = personal(Rol.TECNICO);
        assertThatThrownBy(() -> servicio.confirmarPago(pedido.id())).isInstanceOf(SinPermisoException.class);
        assertThatThrownBy(() -> servicio.cancelar(pedido.id(), "x")).isInstanceOf(SinPermisoException.class);
        assertThat(servicio.obtener(pedido.id())).isPresent(); // pero sí lo puede ver
    }

    @Test
    void rechazarElComprobanteLePermiteAlClienteSubirOtro() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        servicio.informarPago(pedido.id(), comprobante());

        logueado = personal(Rol.DUENO);
        servicio.rechazarComprobante(pedido.id(), "Es de otro monto");

        logueado = ana;
        DetalleDePedido visto = servicio.miPedido(pedido.id()).orElseThrow();
        assertThat(visto.estado()).isEqualTo(EstadoPedido.ESPERANDO_PAGO);
        assertThat(visto.comprobantes().getFirst().motivoRechazo()).isEqualTo("Es de otro monto");
        assertThat(visto.puedeSubirComprobante()).isTrue();
    }

    // ─── Stock y clientes (ADR-014) ──────────────────────────────────────────────────────────

    @Test
    void siNoAlcanzaElStockLaLineaQuedaComoReserva_peroSePuedeComprar() {
        stock.hay.put(KIT.id(), 1);          // hay 1, pide 2
        stock.hay.put(RYZEN_5600.id(), 5);   // de este sobra
        // El resto de los componentes no tiene control de stock: nunca falta.

        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 2), pcAm4(1)));

        assertThat(pedido.lineas().get(0).reserva()).isTrue();
        assertThat(pedido.lineas().get(1).reserva()).isFalse();
        assertThat(pedido.lineas().get(2).reserva()).isFalse(); // el armado no lleva productos
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ESPERANDO_PAGO);
    }

    @Test
    void elStockSeMiraSumandoTodoElPedido() {
        stock.hay.put(RYZEN_5600.id(), 1);   // dos PCs con el mismo procesador piden 2

        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.PcArmada(piezasAm4(), false, false, 2)));

        assertThat(pedido.lineas().getFirst().reserva()).isTrue();
    }

    @Test
    void alConfirmarElPagoSeDescuentaCadaProducto_yLaPcPorComponentes() {
        DetalleDePedido pedido = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 2), pcAm4(1)));
        assertThat(stock.descontados).isEmpty(); // pedir no descuenta: recién al confirmar el pago

        logueado = personal(Rol.MOSTRADOR);
        servicio.confirmarPago(pedido.id());

        assertThat(stock.descontados).hasSize(1);
        Map<com.ram.catalogo.ProductoId, Integer> descontado = stock.descontados.getFirst();
        assertThat(descontado.get(KIT.id())).isEqualTo(2);
        assertThat(descontado.get(RYZEN_5600.id())).isEqualTo(1);
        assertThat(descontado.get(com.ram.pedidos.Productos.GABINETE_MATX.id())).isEqualTo(1);
        assertThat(descontado).hasSize(8); // el kit + 7 componentes (el armado no es un producto)
    }

    @Test
    void cancelarUnPedidoPagadoDevuelveElStock_unoSinPagarNo() {
        DetalleDePedido pagado = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        DetalleDePedido sinPagar = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        logueado = personal(Rol.DUENO);
        servicio.confirmarPago(pagado.id());

        servicio.cancelar(sinPagar.id(), "No contesta");
        assertThat(stock.devueltos).isEmpty();

        servicio.cancelar(pagado.id(), "Se arrepintió");
        assertThat(stock.devueltos).containsExactly(pagado.id().valor());
    }

    @Test
    void soloElDuenoBorraPedidos_ySiEstabaPagadoDevuelveElStock() {
        DetalleDePedido pagado = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        DetalleDePedido sinPagar = servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        logueado = personal(Rol.MOSTRADOR);
        servicio.confirmarPago(pagado.id());
        assertThatThrownBy(() -> servicio.borrar(pagado.id())).isInstanceOf(SinPermisoException.class);

        logueado = personal(Rol.DUENO);
        servicio.borrar(sinPagar.id());
        assertThat(stock.devueltos).isEmpty();
        servicio.borrar(pagado.id());
        assertThat(stock.devueltos).containsExactly(pagado.id().valor());
        assertThat(pedidos.pedidos.values()).isEmpty();
    }

    @Test
    void quienCompraQuedaVinculadoASuFichaDeCliente() {
        servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        assertThat(clientes.vinculados).containsExactly(ana.id() + " 341 555-1234");
    }

    @Test
    void desdeLaFichaSeVenLosPedidosDeUnaCuenta() {
        servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));
        UsuarioAutenticado otro = cliente("Otro");
        logueado = otro;
        servicio.realizar(pedido(new ItemPedido.Producto("kit-4-camaras", 1)));

        logueado = personal(Rol.TECNICO);
        assertThat(servicio.deCuenta(ana.id())).hasSize(1);
        logueado = ana;
        assertThatThrownBy(() -> servicio.deCuenta(ana.id())).isInstanceOf(SinPermisoException.class);
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private static NuevoPedido pedido(ItemPedido... items) {
        return new NuevoPedido(List.of(items), FormaDeEntrega.RETIRO, null, "341 555-1234", null);
    }

    private static Map<TipoComponente, String> piezasAm4() {
        Map<TipoComponente, String> piezas = new EnumMap<>(TipoComponente.class);
        piezas.put(TipoComponente.CPU, "ryzen-5-5600");
        piezas.put(TipoComponente.MOTHER, "a520m");
        piezas.put(TipoComponente.RAM, "ddr4-16");
        piezas.put(TipoComponente.GPU, "rtx-4060");
        piezas.put(TipoComponente.ALMACENAMIENTO, "nvme-1tb");
        piezas.put(TipoComponente.FUENTE, "fuente-550");
        piezas.put(TipoComponente.GABINETE, "gabinete-matx");
        return piezas;
    }

    private static ItemPedido.PcArmada pcAm4(int cantidad) {
        return new ItemPedido.PcArmada(piezasAm4(), false, true, cantidad);
    }

    private static ArchivoSubido comprobante() {
        return new ArchivoSubido("transferencia.png", TipoDeArchivo.PNG, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
    }

    private static UsuarioAutenticado cliente(String nombre) {
        return new UsuarioAutenticado(UUID.randomUUID(), TALLER, nombre, TipoDeCuenta.CLIENTE, null);
    }

    private static UsuarioAutenticado personal(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), TALLER, "Personal", TipoDeCuenta.PERSONAL, rol);
    }
}
