package com.ram.pedidos.aplicacion;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.VistaPreviaDeAumento;
import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.ResumenDePedido;
import com.ram.pedidos.dominio.Pedido;
import com.ram.stock.DatosMovimiento;
import com.ram.stock.DatosReserva;
import com.ram.stock.DisponibilidadPublica;
import com.ram.stock.FilaDeStock;
import com.ram.stock.Stock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Versiones en memoria de lo que necesita el servicio de pedidos. */
final class DoblesDePedidos {

    private DoblesDePedidos() {
    }

    static final class PedidosEnMemoria implements PedidosRepositorio {

        final Map<PedidoId, Pedido> pedidos = new LinkedHashMap<>();
        final Map<UUID, byte[]> archivos = new HashMap<>();
        DatosDePago datosDePago = new DatosDePago("ram.nh", "Agustin Negri Hrytezuk", "Naranja X", null);
        Long precioDelArmado = 30_000L;

        @Override
        public void guardar(Pedido pedido) {
            pedidos.put(pedido.id(), pedido);
        }

        @Override
        public void borrar(PedidoId id) {
            pedidos.remove(id);
        }

        @Override
        public void guardarArchivo(UUID comprobante, byte[] contenido) {
            archivos.put(comprobante, contenido);
        }

        @Override
        public Optional<Pedido> porId(PedidoId id) {
            return Optional.ofNullable(pedidos.get(id));
        }

        @Override
        public Optional<byte[]> archivo(UUID comprobante) {
            return Optional.ofNullable(archivos.get(comprobante));
        }

        @Override
        public int siguienteNumero() {
            return pedidos.values().stream().mapToInt(Pedido::numero).max().orElse(0) + 1;
        }

        @Override
        public int cantidadEsperandoPago(UUID cuenta) {
            return (int) pedidos.values().stream()
                    .filter(p -> p.esDe(cuenta) && p.estado() == EstadoPedido.ESPERANDO_PAGO).count();
        }

        @Override
        public List<ResumenDePedido> resumenes(Set<EstadoPedido> estados, int limite) {
            return pedidos.values().stream().filter(p -> estados.contains(p.estado()))
                    .sorted(Comparator.comparingInt(Pedido::numero).reversed()).limit(limite).map(PedidosEnMemoria::resumen).toList();
        }

        @Override
        public List<ResumenDePedido> resumenesDeCuenta(UUID cuenta, int limite) {
            return pedidos.values().stream().filter(p -> p.esDe(cuenta))
                    .sorted(Comparator.comparingInt(Pedido::numero).reversed()).limit(limite).map(PedidosEnMemoria::resumen).toList();
        }

        @Override
        public Optional<DatosDePago> datosDePago() {
            return Optional.ofNullable(datosDePago);
        }

        @Override
        public Optional<Long> precioDelArmado() {
            return Optional.ofNullable(precioDelArmado);
        }

        @Override
        public void guardarPrecioDelArmado(Long precio) {
            precioDelArmado = precio;
        }

        private static ResumenDePedido resumen(Pedido p) {
            return new ResumenDePedido(p.id(), p.numero(), p.clienteNombre(), p.telefono(), p.entrega().forma(), p.total(),
                    p.estado(), p.creadoEn());
        }
    }

    /** Solo lo que usan los pedidos: productos publicados y componentes. */
    static final class CatalogoFalso implements Catalogo {

        private final List<DatosProducto> publicados;
        private final List<DatosProducto> componentes;

        CatalogoFalso(List<DatosProducto> publicados, List<DatosProducto> componentes) {
            this.publicados = publicados;
            this.componentes = componentes;
        }

        @Override
        public Optional<DatosProducto> obtenerPublicado(String slug) {
            return publicados.stream().filter(p -> p.slug().equals(slug)).findFirst();
        }

        @Override
        public List<DatosProducto> componentes() {
            return componentes;
        }

        @Override
        public List<DatosCategoria> categorias() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosProducto> buscar(FiltroCatalogo filtro) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosProducto> listarParaPrecios() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cambiarPrecio(ProductoId id, Long nuevoPrecio) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VistaPreviaDeAumento simularAumento(BigDecimal porcentaje, String categoria) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int aplicarAumento(BigDecimal porcentaje, String categoria) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosCambioDePrecio> historialDePrecios(ProductoId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<DatosProducto> obtener(ProductoId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProductoId crearProducto(com.ram.catalogo.FichaDeProducto ficha, Long precio) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void editarProducto(ProductoId id, com.ram.catalogo.FichaDeProducto ficha) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cambiarOferta(ProductoId id, Long precioOferta, java.time.LocalDate hasta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UUID agregarFoto(ProductoId id, com.ram.archivos.ArchivoSubido imagen) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void borrarFoto(ProductoId id, UUID foto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ordenarFotos(ProductoId id, List<UUID> fotos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.ram.catalogo.ArchivoDeFoto> foto(UUID foto, boolean chica) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DatosCategoria crearCategoria(String nombre, boolean conInstalacion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void editarCategoria(String slug, String nombre, boolean conInstalacion) {
            throw new UnsupportedOperationException();
        }
    }

    /** Stock en memoria: cuánto hay (solo los controlados) y qué le pidieron descontar o devolver. */
    static final class StockFalso implements Stock {

        final Map<ProductoId, Integer> hay = new HashMap<>();
        final List<Map<ProductoId, Integer>> descontados = new ArrayList<>();
        final List<UUID> devueltos = new ArrayList<>();

        @Override
        public Map<ProductoId, Integer> disponibles(Collection<ProductoId> productos) {
            Map<ProductoId, Integer> encontrados = new HashMap<>();
            productos.forEach(p -> {
                if (hay.containsKey(p)) {
                    encontrados.put(p, hay.get(p));
                }
            });
            return encontrados;
        }

        @Override
        public void descontarPorVenta(UUID pedido, int numeroDePedido, Map<ProductoId, Integer> unidades) {
            descontados.add(Map.copyOf(unidades));
        }

        @Override
        public void devolverPorCancelacion(UUID pedido, int numeroDePedido) {
            devueltos.add(pedido);
        }

        @Override
        public List<DisponibilidadPublica> paraLaTienda() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FilaDeStock> listar() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void controlar(ProductoId producto, int cantidadInicial) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ingresar(ProductoId producto, int cantidad, String nota) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ajustar(ProductoId producto, int cantidadContada, String motivo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void dejarDeControlar(ProductoId producto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosMovimiento> movimientos(ProductoId producto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosReserva> reservasPendientes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosReserva> reservasDePedido(UUID pedido) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cubrirReserva(UUID reserva) {
            throw new UnsupportedOperationException();
        }
    }

    /** Solo anota a quién se vinculó al comprar. */
    static final class ClientesFalsos implements Clientes {

        final List<String> vinculados = new ArrayList<>();

        @Override
        public Optional<ClienteId> vincularAlComprar(UUID cuenta, String telefono) {
            vinculados.add(cuenta + " " + telefono);
            return Optional.of(ClienteId.nuevo());
        }

        @Override
        public ClienteId registrar(NuevoCliente nuevo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosCliente> buscarPorNombre(String texto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<DatosCliente> obtener(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DatosCliente> obtenerVarios(Collection<ClienteId> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void actualizarContacto(ClienteId id, String telefono, String correo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FichaDeCliente> listar(String texto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<FichaDeCliente> ficha(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void editar(ClienteId id, DatosDeFicha datos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void vincularCuenta(ClienteId id, String correoDeLaCuenta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void desvincularCuenta(ClienteId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ClienteId> deLaCuenta(UUID cuenta) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void borrar(ClienteId id) {
            throw new UnsupportedOperationException();
        }
    }
}
