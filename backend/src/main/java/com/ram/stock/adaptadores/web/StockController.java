package com.ram.stock.adaptadores.web;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.TipoComponente;
import com.ram.stock.DatosMovimiento;
import com.ram.stock.DatosReserva;
import com.ram.stock.DisponibilidadPublica;
import com.ram.stock.EstadoDeReserva;
import com.ram.stock.EstadoDeStock;
import com.ram.stock.FilaDeStock;
import com.ram.stock.OperacionDeStockInvalidaException;
import com.ram.stock.ReservaNoEncontradaException;
import com.ram.stock.SinPermisoException;
import com.ram.stock.Stock;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stock (ADR-014).
 *
 * <pre>
 * GET    /api/publico/stock                               [{slug, estado, quedan}] de los que tienen control
 * GET    /api/sistema/stock                               todos los productos con su stock
 * POST   /api/sistema/stock/{producto}                    {"cantidad": 5}   empezar a controlar
 * POST   /api/sistema/stock/{producto}/ingresos           {"cantidad": 3, "nota": "Factura 123"}
 * PUT    /api/sistema/stock/{producto}                    {"cantidad": 4, "motivo": "Se rompió una"}   ajuste
 * DELETE /api/sistema/stock/{producto}                    dejar de controlar
 * GET    /api/sistema/stock/{producto}/movimientos
 * GET    /api/sistema/stock/reservas                      las pendientes
 * GET    /api/sistema/stock/reservas?pedido={id}          las de un pedido
 * POST   /api/sistema/stock/reservas/{id}/cubrir
 * </pre>
 */
@RestController
class StockController {

    private final Stock stock;

    StockController(Stock stock) {
        this.stock = stock;
    }

    /** Lo pide el servidor de la tienda (que ya cachea 30 s); 30 s más en navegadores y proxies. */
    @GetMapping("/api/publico/stock")
    ResponseEntity<List<DisponibilidadPublica>> paraLaTienda() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic()).body(stock.paraLaTienda());
    }

    @GetMapping("/api/sistema/stock")
    List<FilaJson> listar() {
        return stock.listar().stream().map(FilaJson::de).toList();
    }

    @PostMapping("/api/sistema/stock/{producto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void controlar(@PathVariable UUID producto, @RequestBody CantidadJson cuerpo) {
        stock.controlar(ProductoId.de(producto), cuerpo.cantidadObligatoria());
    }

    @PostMapping("/api/sistema/stock/{producto}/ingresos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ingresar(@PathVariable UUID producto, @RequestBody CantidadJson cuerpo) {
        stock.ingresar(ProductoId.de(producto), cuerpo.cantidadObligatoria(), cuerpo.nota());
    }

    @PutMapping("/api/sistema/stock/{producto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ajustar(@PathVariable UUID producto, @RequestBody CantidadJson cuerpo) {
        stock.ajustar(ProductoId.de(producto), cuerpo.cantidadObligatoria(), cuerpo.motivo());
    }

    @DeleteMapping("/api/sistema/stock/{producto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void dejarDeControlar(@PathVariable UUID producto) {
        stock.dejarDeControlar(ProductoId.de(producto));
    }

    @GetMapping("/api/sistema/stock/{producto}/movimientos")
    List<DatosMovimiento> movimientos(@PathVariable UUID producto) {
        return stock.movimientos(ProductoId.de(producto));
    }

    @GetMapping("/api/sistema/stock/reservas")
    List<ReservaJson> reservas(@RequestParam(required = false) UUID pedido) {
        List<DatosReserva> reservas = pedido == null ? stock.reservasPendientes() : stock.reservasDePedido(pedido);
        return reservas.stream().map(ReservaJson::de).toList();
    }

    @PostMapping("/api/sistema/stock/reservas/{id}/cubrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cubrir(@PathVariable UUID id) {
        stock.cubrirReserva(id);
    }

    // ─── JSON ─────────────────────────────────────────────────────────────────────────────────

    record CantidadJson(Integer cantidad, String nota, String motivo) {
        int cantidadObligatoria() {
            if (cantidad == null) {
                throw new IllegalArgumentException("Falta la cantidad");
            }
            return cantidad;
        }
    }

    /** @param cantidad null = sin control de stock */
    record FilaJson(UUID id, String slug, String nombre, String categoria, TipoComponente tipoComponente, boolean publicado,
                    Integer cantidad, int reservado, EstadoDeStock estado) {
        static FilaJson de(FilaDeStock f) {
            return new FilaJson(f.id().valor(), f.slug(), f.nombre(), f.categoria(), f.tipoComponente(), f.publicado(),
                    f.cantidad(), f.reservado(), f.controlado() ? DisponibilidadPublica.de(f.slug(), f.cantidad()).estado() : null);
        }
    }

    record ReservaJson(UUID id, UUID producto, String productoNombre, UUID pedido, int numeroPedido, int cantidad,
                       EstadoDeReserva estado, Instant creadaEn, Instant resueltaEn, boolean sePuedeCubrir) {
        static ReservaJson de(DatosReserva r) {
            return new ReservaJson(r.id(), r.producto().valor(), r.productoNombre(), r.pedido(), r.numeroPedido(),
                    r.cantidad(), r.estado(), r.creadaEn(), r.resueltaEn(), r.sePuedeCubrir());
        }
    }

    // ─── Errores ──────────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá la cantidad"));
    }

    @ExceptionHandler(OperacionDeStockInvalidaException.class)
    ResponseEntity<Map<String, String>> conflicto(OperacionDeStockInvalidaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "operacion_invalida", "detalle", e.getMessage()));
    }

    @ExceptionHandler({ProductoNoEncontradoException.class, ReservaNoEncontradaException.class})
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "no_encontrado"));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }
}
