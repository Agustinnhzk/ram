package com.ram.stock.aplicacion;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.stock.DatosMovimiento;
import com.ram.stock.DatosReserva;
import com.ram.stock.DisponibilidadPublica;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.FilaDeStock;
import com.ram.stock.OperacionDeStockInvalidaException;
import com.ram.stock.ReservaNoEncontradaException;
import com.ram.stock.SinPermisoException;
import com.ram.stock.Stock;
import com.ram.stock.TipoDeMovimiento;
import com.ram.stock.dominio.Existencia;
import com.ram.stock.dominio.Movimiento;
import com.ram.stock.dominio.Nota;
import com.ram.stock.dominio.Reserva;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Casos de uso de stock (ADR-014).
 *
 * <p>Reglas:
 * <ul>
 *   <li>El stock nunca es negativo. Lo vendido sin stock queda como reserva, que el taller cubre a mano.</li>
 *   <li>Todo cambio deja un movimiento con quién, cuándo y por qué.</li>
 *   <li>Descontar y devolver por un pedido pasa una sola vez por pedido, aunque lo llamen dos veces.</li>
 * </ul>
 */
public class ServicioStock implements Stock {

    static final int MOVIMIENTOS_A_MOSTRAR = 100;

    /** Los movimientos que sacaron unidades para un pedido: es lo que vuelve si se cancela. */
    private static final Set<TipoDeMovimiento> SALIDAS_POR_PEDIDO = EnumSet.of(TipoDeMovimiento.VENTA, TipoDeMovimiento.RESERVA_CUBIERTA);

    private final StockRepositorio stock;
    private final Catalogo catalogo;
    private final SesionActual sesion;
    private final Clock reloj;

    public ServicioStock(StockRepositorio stock, Catalogo catalogo, SesionActual sesion, Clock reloj) {
        this.stock = Objects.requireNonNull(stock);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    // ─── Tienda ───────────────────────────────────────────────────────────────────────────────

    @Override
    public List<DisponibilidadPublica> paraLaTienda() {
        Map<ProductoId, Integer> cantidades = stock.cantidades();
        if (cantidades.isEmpty()) {
            return List.of();
        }
        // Los publicados y los componentes de Armá tu PC (que pueden no estar publicados como productos sueltos).
        return catalogo.listarParaPrecios().stream()
                .filter(p -> p.publicado() || p.esComponente())
                .filter(p -> cantidades.containsKey(p.id()))
                .map(p -> DisponibilidadPublica.de(p.slug(), cantidades.get(p.id())))
                .toList();
    }

    // ─── Pedidos ──────────────────────────────────────────────────────────────────────────────

    @Override
    public Map<ProductoId, Integer> disponibles(Collection<ProductoId> productos) {
        return productos.isEmpty() ? Map.of() : stock.cantidades(productos);
    }

    @Override
    public void descontarPorVenta(UUID pedido, int numeroDePedido, Map<ProductoId, Integer> unidades) {
        Objects.requireNonNull(pedido);
        boolean yaSeDesconto = stock.movimientosDePedido(pedido).stream().anyMatch(m -> m.tipo() == TipoDeMovimiento.VENTA)
                || !stock.reservasDePedido(pedido).isEmpty();
        if (yaSeDesconto) {
            return;
        }
        Instant ahora = reloj.instant();
        String usuario = nombreDeQuienLoHace();
        // Siempre en el mismo orden (por id): dos confirmaciones a la vez bloquean los productos en el mismo orden
        // y no se traban esperándose una a la otra (deadlock).
        for (Map.Entry<ProductoId, Integer> item : ordenadoPorId(unidades).entrySet()) {
            int pedidas = item.getValue();
            if (pedidas <= 0) {
                continue;
            }
            stock.paraModificar(item.getKey()).ifPresent(existencia -> {
                int sacadas = existencia.descontarLoQueHaya(pedidas);
                if (sacadas > 0) {
                    stock.guardar(existencia);
                    stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.VENTA, -sacadas, null, pedido, numeroDePedido,
                            usuario, ahora));
                }
                if (sacadas < pedidas) {
                    stock.guardar(Reserva.nueva(existencia.producto(), pedido, numeroDePedido, pedidas - sacadas, ahora));
                }
            });
        }
    }

    @Override
    public void devolverPorCancelacion(UUID pedido, int numeroDePedido) {
        Objects.requireNonNull(pedido);
        List<Movimiento> movimientos = stock.movimientosDePedido(pedido);
        if (movimientos.stream().anyMatch(m -> m.tipo() == TipoDeMovimiento.DEVOLUCION)) {
            return;
        }
        Instant ahora = reloj.instant();
        String usuario = nombreDeQuienLoHace();

        Map<ProductoId, Integer> salieron = new HashMap<>();
        movimientos.stream().filter(m -> SALIDAS_POR_PEDIDO.contains(m.tipo()))
                .forEach(m -> salieron.merge(m.producto(), -m.cantidad(), Integer::sum));
        for (Map.Entry<ProductoId, Integer> item : ordenadoPorId(salieron).entrySet()) {
            if (item.getValue() <= 0) {
                continue;
            }
            // Si ya no se controla el stock de ese producto, no hay a dónde devolverlo.
            stock.paraModificar(item.getKey()).ifPresent(existencia -> {
                existencia.ingresar(item.getValue());
                stock.guardar(existencia);
                stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.DEVOLUCION, item.getValue(),
                        "Pedido cancelado", pedido, numeroDePedido, usuario, ahora));
            });
        }
        for (Reserva reserva : stock.reservasDePedido(pedido)) {
            if (reserva.pendiente()) {
                reserva.cancelar(ahora);
                stock.guardar(reserva);
            }
        }
    }

    // ─── Sistema ──────────────────────────────────────────────────────────────────────────────

    @Override
    public List<FilaDeStock> listar() {
        personal();
        Map<ProductoId, Integer> cantidades = stock.cantidades();
        Map<ProductoId, Integer> reservado = new HashMap<>();
        stock.reservasPendientes().forEach(r -> reservado.merge(r.producto(), r.cantidad(), Integer::sum));
        return catalogo.listarParaPrecios().stream()
                .map(p -> new FilaDeStock(p.id(), p.slug(), p.nombre(), p.categoria().nombre(),
                        p.tecnicos() == null ? null : p.tecnicos().tipo(), p.publicado(),
                        cantidades.get(p.id()), reservado.getOrDefault(p.id(), 0)))
                .toList();
    }

    @Override
    public void controlar(ProductoId producto, int cantidadInicial) {
        UsuarioAutenticado usuario = manejaStock();
        exigirProducto(producto);
        if (stock.paraModificar(producto).isPresent()) {
            throw new OperacionDeStockInvalidaException("Ese producto ya tiene control de stock");
        }
        Existencia existencia = Existencia.empezar(producto, cantidadInicial);
        stock.guardar(existencia);
        stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.INICIAL, cantidadInicial, null, null, null,
                usuario.nombre(), reloj.instant()));
    }

    @Override
    public void ingresar(ProductoId producto, int cantidad, String nota) {
        UsuarioAutenticado usuario = manejaStock();
        String limpia = Nota.opcional(nota);
        Existencia existencia = controlada(producto);
        existencia.ingresar(cantidad);
        stock.guardar(existencia);
        stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.INGRESO, cantidad, limpia, null, null,
                usuario.nombre(), reloj.instant()));
    }

    @Override
    public void ajustar(ProductoId producto, int cantidadContada, String motivo) {
        UsuarioAutenticado usuario = manejaStock();
        String limpio = Nota.obligatoria(motivo, "el motivo del ajuste");
        Existencia existencia = controlada(producto);
        int diferencia = existencia.ajustar(cantidadContada);
        if (diferencia == 0) {
            return;
        }
        stock.guardar(existencia);
        stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.AJUSTE, diferencia, limpio, null, null,
                usuario.nombre(), reloj.instant()));
    }

    @Override
    public void dejarDeControlar(ProductoId producto) {
        if (!personal().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede dejar de controlar el stock de un producto");
        }
        controlada(producto);
        if (stock.reservasPendientes().stream().anyMatch(r -> r.producto().equals(producto))) {
            throw new OperacionDeStockInvalidaException("Tiene reservas pendientes: cubrilas o cancelá esos pedidos antes");
        }
        // Los movimientos quedan: son historia. Si vuelve a controlarse, arranca con un INICIAL nuevo.
        stock.borrar(producto);
    }

    @Override
    public List<DatosMovimiento> movimientos(ProductoId producto) {
        personal();
        return stock.movimientos(producto, MOVIMIENTOS_A_MOSTRAR).stream()
                .map(m -> new DatosMovimiento(m.fecha(), m.tipo(), m.cantidad(), m.saldo(), m.nota(), m.numeroPedido(), m.usuario()))
                .toList();
    }

    @Override
    public List<DatosReserva> reservasPendientes() {
        personal();
        return aDatos(stock.reservasPendientes());
    }

    @Override
    public List<DatosReserva> reservasDePedido(UUID pedido) {
        personal();
        return aDatos(stock.reservasDePedido(pedido));
    }

    @Override
    public void cubrirReserva(UUID id) {
        UsuarioAutenticado usuario = manejaStock();
        Reserva reserva = stock.reserva(id).orElseThrow(ReservaNoEncontradaException::new);
        if (!reserva.pendiente()) {
            throw new OperacionDeStockInvalidaException("La reserva ya está "
                    + (reserva.estado() == EstadoDeReserva.CUBIERTA ? "cubierta" : "cancelada"));
        }
        Existencia existencia = stock.paraModificar(reserva.producto()).orElseThrow(() ->
                new OperacionDeStockInvalidaException("Ese producto ya no tiene control de stock"));
        existencia.sacar(reserva.cantidad());
        Instant ahora = reloj.instant();
        reserva.cubrir(ahora);
        stock.guardar(existencia);
        stock.guardar(reserva);
        stock.agregar(Movimiento.de(existencia, TipoDeMovimiento.RESERVA_CUBIERTA, -reserva.cantidad(), null,
                reserva.pedido(), reserva.numeroPedido(), usuario.nombre(), ahora));
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private List<DatosReserva> aDatos(List<Reserva> reservas) {
        if (reservas.isEmpty()) {
            return List.of();
        }
        Map<ProductoId, String> nombres = catalogo.listarParaPrecios().stream()
                .collect(Collectors.toMap(DatosProducto::id, DatosProducto::nombre, (a, b) -> a));
        Map<ProductoId, Integer> cantidades = stock.cantidades(reservas.stream().map(Reserva::producto).collect(Collectors.toSet()));
        return reservas.stream()
                .sorted(Comparator.comparing(Reserva::creadaEn))
                .map(r -> new DatosReserva(r.id(), r.producto(), nombres.getOrDefault(r.producto(), "Producto borrado"),
                        r.pedido(), r.numeroPedido(), r.cantidad(), r.estado(), r.creadaEn(), r.resueltaEn(),
                        r.pendiente() && cantidades.getOrDefault(r.producto(), 0) >= r.cantidad()))
                .toList();
    }

    private Existencia controlada(ProductoId producto) {
        return stock.paraModificar(producto).orElseThrow(() ->
                new OperacionDeStockInvalidaException("Ese producto no tiene control de stock: activalo primero"));
    }

    private void exigirProducto(ProductoId producto) {
        if (catalogo.obtener(producto).isEmpty()) {
            throw new ProductoNoEncontradoException(producto);
        }
    }

    private static <V> Map<ProductoId, V> ordenadoPorId(Map<ProductoId, V> mapa) {
        Map<ProductoId, V> ordenado = new TreeMap<>(Comparator.comparing(ProductoId::valor));
        ordenado.putAll(mapa);
        return ordenado;
    }

    /** Lo llama pedidos con alguien logueado (quien confirma o cancela); por las dudas, sin romper si no hay nadie. */
    private String nombreDeQuienLoHace() {
        return sesion.usuario().map(UsuarioAutenticado::nombre).orElse("Sistema");
    }

    private UsuarioAutenticado personal() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (!usuario.esPersonal()) {
            throw new SinPermisoException("Solo el personal del taller");
        }
        return usuario;
    }

    /** Cargar y ajustar stock: Dueño y Mostrador (no el Técnico). */
    private UsuarioAutenticado manejaStock() {
        UsuarioAutenticado usuario = personal();
        if (!usuario.tieneAlgunRol(Rol.DUENO, Rol.MOSTRADOR)) {
            throw new SinPermisoException("El stock lo cargan el Dueño y Mostrador");
        }
        return usuario;
    }
}
