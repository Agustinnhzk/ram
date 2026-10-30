package com.ram.pedidos.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.ProductoId;
import com.ram.clientes.Clientes;
import com.ram.catalogo.TipoComponente;
import com.ram.pedidos.ArchivoDeComprobante;
import com.ram.pedidos.Cotizacion;
import com.ram.pedidos.DatosComprobante;
import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.DetalleDePedido;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.FormaDeEntrega;
import com.ram.pedidos.ItemPedido;
import com.ram.pedidos.LineaDePedido;
import com.ram.pedidos.NuevoPedido;
import com.ram.pedidos.OperacionNoPermitidaException;
import com.ram.pedidos.PagoInformado;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.PedidoNoEncontradoException;
import com.ram.pedidos.PedidoRealizado;
import com.ram.pedidos.Pedidos;
import com.ram.pedidos.ProductoNoDisponibleException;
import com.ram.pedidos.ResumenDePedido;
import com.ram.pedidos.SinPermisoException;
import com.ram.pedidos.dominio.ArmadoDePc;
import com.ram.pedidos.dominio.Comprobante;
import com.ram.pedidos.dominio.Entrega;
import com.ram.pedidos.dominio.Linea;
import com.ram.pedidos.dominio.Pedido;
import com.ram.stock.Stock;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;

import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Casos de uso de pedidos (ADR-011).
 *
 * <p>Regla de oro: <b>el precio lo pone el servidor</b>. Del carrito solo se toma QUÉ quiere el cliente
 * (slugs y cantidades); los precios salen del catálogo en este momento.
 */
public class ServicioPedidos implements Pedidos {

    /** Pedidos sin pagar que puede tener abiertos un cliente. Frena a quien llena el sistema de pedidos falsos. */
    static final int PEDIDOS_SIN_PAGAR_MAXIMOS = 5;
    static final int LIMITE_LISTADO = 200;
    static final Set<TipoDeArchivo> TIPOS_DE_COMPROBANTE = EnumSet.of(TipoDeArchivo.PDF, TipoDeArchivo.JPG, TipoDeArchivo.PNG);

    private final PedidosRepositorio pedidos;
    private final Catalogo catalogo;
    private final Stock stock;
    private final Clientes clientes;
    private final SesionActual sesion;
    private final PublicadorDeEventos eventos;
    private final Clock reloj;

    public ServicioPedidos(PedidosRepositorio pedidos, Catalogo catalogo, Stock stock, Clientes clientes, SesionActual sesion,
                           PublicadorDeEventos eventos, Clock reloj) {
        this.pedidos = Objects.requireNonNull(pedidos);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.stock = Objects.requireNonNull(stock);
        this.clientes = Objects.requireNonNull(clientes);
        this.sesion = Objects.requireNonNull(sesion);
        this.eventos = Objects.requireNonNull(eventos);
        this.reloj = Objects.requireNonNull(reloj);
    }

    // ─── Tienda ───────────────────────────────────────────────────────────────────────────────

    @Override
    public Optional<DatosDePago> datosDePago() {
        return pedidos.datosDePago();
    }

    @Override
    public Optional<Long> precioDelArmado() {
        return pedidos.precioDelArmado();
    }

    @Override
    public void cambiarPrecioDelArmado(Long precio) {
        if (!personal().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede cambiar el precio del armado");
        }
        if (precio != null && (precio <= 0 || precio > 100_000_000L)) {
            throw new IllegalArgumentException("El precio del armado tiene que ser mayor a cero");
        }
        pedidos.guardarPrecioDelArmado(precio);
    }

    @Override
    public Cotizacion cotizar(NuevoPedido nuevo) {
        cliente();
        Entrega entrega = Entrega.de(nuevo.entrega(), nuevo.direccion());
        List<Linea> lineas = lineas(nuevo.items());
        return new Cotizacion(lineas.stream().map(ServicioPedidos::aLinea).toList(),
                lineas.stream().mapToLong(Linea::subtotal).reduce(0, Math::addExact),
                entrega.forma() == FormaDeEntrega.ENVIO_OTRA_LOCALIDAD);
    }

    @Override
    public DetalleDePedido realizar(NuevoPedido nuevo) {
        UsuarioAutenticado cliente = cliente();
        Entrega entrega = Entrega.de(nuevo.entrega(), nuevo.direccion());
        List<Linea> lineas = lineas(nuevo.items());
        if (pedidos.cantidadEsperandoPago(cliente.id()) >= PEDIDOS_SIN_PAGAR_MAXIMOS) {
            throw new OperacionNoPermitidaException("Tenés varios pedidos esperando el pago. Pagá o cancelá alguno antes de hacer otro.");
        }

        Pedido pedido = Pedido.crear(pedidos.siguienteNumero(), cliente.id(), cliente.nombre(), nuevo.telefono(), entrega,
                nuevo.nota(), lineas, reloj.instant());
        pedidos.guardar(pedido);
        // Quien compra queda en la ficha de clientes del taller, vinculada a su cuenta (ADR-014).
        clientes.vincularAlComprar(cliente.id(), pedido.telefono());
        eventos.publicar(new PedidoRealizado(pedido.numero(), pedido.clienteNombre(), pedido.telefono(),
                entrega.forma(), entrega.direccion(), pedido.nota(), textoDeLineas(pedido), pedido.total()));
        return detalle(pedido);
    }

    @Override
    public List<ResumenDePedido> misPedidos() {
        return pedidos.resumenesDeCuenta(cliente().id(), LIMITE_LISTADO);
    }

    @Override
    public Optional<DetalleDePedido> miPedido(PedidoId id) {
        return delCliente(id).map(this::detalle);
    }

    @Override
    public void informarPago(PedidoId id, ArchivoSubido archivo) {
        Objects.requireNonNull(archivo);
        if (!TIPOS_DE_COMPROBANTE.contains(archivo.tipo())) {
            throw new IllegalArgumentException("El comprobante tiene que ser una imagen (JPG o PNG) o un PDF");
        }
        Pedido pedido = delCliente(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        Comprobante comprobante = new Comprobante(UUID.randomUUID(), archivo.nombre(), archivo.tipo(), archivo.tamano(),
                reloj.instant(), false, null);
        pedido.informarPago(comprobante);
        pedidos.guardar(pedido);
        pedidos.guardarArchivo(comprobante.id(), archivo.contenido());
        eventos.publicar(new PagoInformado(pedido.numero(), pedido.clienteNombre(), pedido.telefono(), pedido.total()));
    }

    @Override
    public void cancelarMiPedido(PedidoId id) {
        Pedido pedido = delCliente(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        pedido.cancelarPorElCliente(reloj.instant());
        pedidos.guardar(pedido);
    }

    // ─── Sistema ──────────────────────────────────────────────────────────────────────────────

    @Override
    public List<ResumenDePedido> listar(EstadoPedido estado) {
        personal();
        return pedidos.resumenes(estado == null ? EstadoPedido.ACTIVOS : EnumSet.of(estado), LIMITE_LISTADO);
    }

    @Override
    public List<ResumenDePedido> deCuenta(UUID cuenta) {
        personal();
        return pedidos.resumenesDeCuenta(cuenta, LIMITE_LISTADO);
    }

    @Override
    public Optional<DetalleDePedido> obtener(PedidoId id) {
        personal();
        return pedidos.porId(id).map(this::detalle);
    }

    @Override
    public Optional<ArchivoDeComprobante> comprobante(PedidoId id, UUID comprobante) {
        personal();
        return pedidos.porId(id)
                .flatMap(p -> p.comprobantes().stream().filter(c -> c.id().equals(comprobante)).findFirst())
                .flatMap(c -> pedidos.archivo(c.id())
                        .map(bytes -> new ArchivoDeComprobante(c.nombre(), c.tipo().tipoDeContenido(), bytes)));
    }

    @Override
    public void confirmarPago(PedidoId id) {
        manejaPlata();
        Pedido pedido = modificar(id, p -> p.confirmarPago(reloj.instant()));
        // En la misma transacción: si falla el stock, tampoco queda confirmado el pago.
        Map<ProductoId, Integer> unidades = new LinkedHashMap<>();
        pedido.unidades().forEach((producto, cantidad) -> unidades.put(ProductoId.de(producto), cantidad));
        if (!unidades.isEmpty()) {
            stock.descontarPorVenta(pedido.id().valor(), pedido.numero(), unidades);
        }
    }

    @Override
    public void rechazarComprobante(PedidoId id, String motivo) {
        manejaPlata();
        modificar(id, p -> p.rechazarComprobante(motivo));
    }

    @Override
    public void entregar(PedidoId id) {
        personal();
        modificar(id, p -> p.entregar(reloj.instant()));
    }

    @Override
    public void cancelar(PedidoId id, String motivo) {
        manejaPlata();
        Pedido pedido = pedidos.porId(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        boolean estabaPagado = pedido.estado() == EstadoPedido.PAGADO;
        pedido.cancelar(motivo, reloj.instant());
        pedidos.guardar(pedido);
        if (estabaPagado) {
            stock.devolverPorCancelacion(pedido.id().valor(), pedido.numero());
        }
    }

    @Override
    public void borrar(PedidoId id) {
        if (!personal().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede borrar pedidos");
        }
        Pedido pedido = pedidos.porId(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        if (pedido.estado() == EstadoPedido.PAGADO) {
            stock.devolverPorCancelacion(pedido.id().valor(), pedido.numero());
        }
        pedidos.borrar(id);
    }

    // ─── Armar las líneas con los precios del catálogo ───────────────────────────────────────

    private List<Linea> lineas(List<ItemPedido> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }
        if (items.size() > Pedido.LINEAS_MAXIMAS) {
            throw new IllegalArgumentException("Un pedido no puede tener más de " + Pedido.LINEAS_MAXIMAS + " productos distintos");
        }
        // Los componentes se buscan una sola vez, aunque el carrito tenga varias PCs.
        Map<String, DatosProducto> componentes = null;
        List<Linea> lineas = new ArrayList<>();
        for (ItemPedido item : items) {
            switch (item) {
                case ItemPedido.Producto p -> lineas.add(lineaDeProducto(p));
                case ItemPedido.PcArmada pc -> {
                    if (componentes == null) {
                        componentes = catalogo.componentes().stream()
                                .collect(Collectors.toMap(DatosProducto::slug, Function.identity(), (a, b) -> a));
                    }
                    lineas.addAll(lineasDePc(pc, componentes));
                }
            }
        }
        if (lineas.size() > Pedido.LINEAS_MAXIMAS) {
            throw new IllegalArgumentException("Un pedido no puede tener más de " + Pedido.LINEAS_MAXIMAS + " productos distintos");
        }
        return marcarSinStock(lineas);
    }

    /**
     * Marca las líneas que llevan algún producto del que no alcanza el stock, sumando todo el pedido
     * (dos PCs con el mismo procesador piden 2). Se pueden comprar igual: quedan como reserva (ADR-014).
     * Es un aviso: el stock recién se descuenta al confirmar el pago.
     */
    private List<Linea> marcarSinStock(List<Linea> lineas) {
        Map<UUID, Integer> pedidas = new HashMap<>();
        for (Linea l : lineas) {
            l.productos().forEach(p -> pedidas.merge(p, l.cantidad(), Integer::sum));
        }
        if (pedidas.isEmpty()) {
            return lineas;
        }
        Map<ProductoId, Integer> hay = stock.disponibles(pedidas.keySet().stream().map(ProductoId::de).toList());
        return lineas.stream()
                .map(l -> l.conSinStock(l.productos().stream().anyMatch(p -> {
                    Integer disponibles = hay.get(ProductoId.de(p));
                    return disponibles != null && disponibles < pedidas.get(p);
                })))
                .toList();
    }

    private Linea lineaDeProducto(ItemPedido.Producto item) {
        String slug = item.slug() == null ? "" : item.slug();
        DatosProducto producto = catalogo.obtenerPublicado(slug).orElseThrow(() ->
                new ProductoNoDisponibleException("Uno de los productos del carrito ya no está disponible. Sacalo y volvé a intentar."));
        // El precio de HOY: si está en oferta, el de oferta (ADR-013).
        if (producto.precioVigente() == null) {
            throw new ProductoNoDisponibleException(producto.nombre() + " no tiene precio publicado: consultanos por WhatsApp.");
        }
        return new Linea(producto.nombre(), List.of(), "/productos/" + producto.slug(), item.cantidad(), producto.precioVigente(),
                List.of(producto.id().valor()), false);
    }

    /**
     * Una PC son una o dos líneas: los componentes, y (si lo pidió) el armado y prueba, con el precio del taller.
     * Separadas, el cliente ve claro cuánto es cada cosa.
     */
    private List<Linea> lineasDePc(ItemPedido.PcArmada item, Map<String, DatosProducto> componentes) {
        Map<TipoComponente, DatosProducto> piezas = new EnumMap<>(TipoComponente.class);
        for (Map.Entry<TipoComponente, String> pieza : item.piezas().entrySet()) {
            DatosProducto componente = componentes.get(pieza.getValue());
            if (componente == null) {
                throw new ProductoNoDisponibleException("Un componente de tu PC ya no está disponible. Volvé a armarla.");
            }
            piezas.put(pieza.getKey(), componente);
        }
        ArmadoDePc armado = ArmadoDePc.revisar(piezas, item.sinPlacaDeVideo());
        Long precio = armado.precio();
        if (precio == null) {
            throw new ProductoNoDisponibleException("Estos componentes no tienen precio publicado: "
                    + String.join(", ", armado.piezasSinPrecio()) + ". Consultanos por WhatsApp.");
        }
        String enlace = armado.enlace() + (item.conArmado() ? "" : "&armado=no");
        List<UUID> componentesDeLaPc = piezas.values().stream().map(p -> p.id().valor()).toList();
        if (!item.conArmado()) {
            return List.of(new Linea("Componentes para armar tu PC", armado.detalle(), enlace, item.cantidad(), precio,
                    componentesDeLaPc, false));
        }
        long precioDelArmado = pedidos.precioDelArmado().orElseThrow(() ->
                new ProductoNoDisponibleException("Por ahora no ofrecemos el armado. Podés comprar los componentes sueltos."));
        return List.of(
                new Linea("PC armada a medida", armado.detalle(), enlace, item.cantidad(), precio, componentesDeLaPc, false),
                new Linea("Armado y prueba de la PC", List.of(), enlace, item.cantidad(), precioDelArmado));
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private Pedido modificar(PedidoId id, java.util.function.Consumer<Pedido> cambio) {
        Pedido pedido = pedidos.porId(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        cambio.accept(pedido);
        pedidos.guardar(pedido);
        return pedido;
    }

    /** El pedido, si es del cliente logueado. Si es de otro, como si no existiera (no se revela que existe). */
    private Optional<Pedido> delCliente(PedidoId id) {
        UUID cuenta = cliente().id();
        return pedidos.porId(id).filter(p -> p.esDe(cuenta));
    }

    private UsuarioAutenticado cliente() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (usuario.esPersonal()) {
            throw new SinPermisoException("Los pedidos de la tienda los hace un cliente");
        }
        return usuario;
    }

    private UsuarioAutenticado personal() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (!usuario.esPersonal()) {
            throw new SinPermisoException("Solo el personal del taller");
        }
        return usuario;
    }

    /** Confirmar, rechazar o cancelar tiene que ver con plata: Dueño y Mostrador, no Técnico. */
    private void manejaPlata() {
        if (!personal().tieneAlgunRol(Rol.DUENO, Rol.MOSTRADOR)) {
            throw new SinPermisoException("Los pagos los manejan el Dueño y Mostrador");
        }
    }

    private DetalleDePedido detalle(Pedido p) {
        return new DetalleDePedido(
                p.id(), p.numero(), p.clienteNombre(), p.telefono(), p.entrega().forma(), p.entrega().direccion(), p.nota(),
                p.lineas().stream().map(ServicioPedidos::aLinea).toList(),
                p.total(), p.estado(), p.creadoEn(), p.pagadoEn(), p.entregadoEn(), p.canceladoEn(), p.motivoCancelacion(),
                p.comprobantes().stream().map(c -> new DatosComprobante(c.id(), c.nombre(), c.tipo().tipoDeContenido(),
                        c.tamano(), c.subidoEn(), c.rechazado(), c.motivoRechazo())).toList(),
                pedidos.datosDePago().orElse(null),
                p.puedeSubirComprobante(),
                p.puedeCancelarElCliente());
    }

    private static LineaDePedido aLinea(Linea l) {
        return new LineaDePedido(l.descripcion(), l.detalle(), l.referencia(), l.cantidad(), l.precioUnitario(), l.subtotal(),
                l.sinStock());
    }

    private static List<String> textoDeLineas(Pedido p) {
        List<String> texto = new ArrayList<>();
        for (Linea l : p.lineas()) {
            texto.add(l.cantidad() + " × " + l.descripcion() + " — " + pesos(l.subtotal()) + (l.sinStock() ? "  (SIN STOCK: reserva)" : ""));
            l.detalle().forEach(d -> texto.add("    " + d));
        }
        return texto;
    }

    /** 1234567 → "$ 1.234.567", como en la tienda. */
    static String pesos(long monto) {
        return "$ " + java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("es-AR")).format(monto);
    }
}
