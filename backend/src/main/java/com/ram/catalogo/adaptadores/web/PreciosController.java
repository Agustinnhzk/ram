package com.ram.catalogo.adaptadores.web;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.PrecioPropuesto;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.VistaPreviaDeAumento;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * La página de precios del sistema de gestión. Solo personal logueado (regla de SeguridadConfig);
 * cambiar precios, además, solo el Dueño (regla del servicio → 403).
 *
 * <pre>
 * GET  /api/sistema/precios                         todos los productos con su precio
 * PUT  /api/sistema/precios/{id}                    {"precio": 45000}  o  {"aConsultar": true}
 * POST /api/sistema/precios/aumento/vista-previa    {"porcentaje": 8, "categoria": "camaras"}  (categoria opcional)
 * POST /api/sistema/precios/aumento                 lo mismo, pero lo aplica
 * GET  /api/sistema/precios/{id}/historial          últimos cambios de ese producto
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/precios")
class PreciosController {

    private final Catalogo catalogo;

    PreciosController(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    List<PrecioJson> listar() {
        return catalogo.listarParaPrecios().stream().map(PrecioJson::de).toList();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiar(@PathVariable UUID id, @RequestBody PrecioNuevo pedido) {
        // Pasar a "a consultar" tiene que ser explícito: un {} vacío o un precio olvidado
        // no puede borrar el precio de un producto por accidente.
        boolean aConsultar = Boolean.TRUE.equals(pedido.aConsultar());
        if (!aConsultar && pedido.precio() == null) {
            throw new IllegalArgumentException("Falta el precio");
        }
        catalogo.cambiarPrecio(ProductoId.de(id), aConsultar ? null : pedido.precio());
    }

    @PostMapping("/aumento/vista-previa")
    VistaPreviaJson vistaPrevia(@RequestBody Aumento pedido) {
        return VistaPreviaJson.de(catalogo.simularAumento(pedido.porcentaje(), vacioANull(pedido.categoria())));
    }

    @PostMapping("/aumento")
    Map<String, Integer> aplicar(@RequestBody Aumento pedido) {
        return Map.of("cambiados", catalogo.aplicarAumento(pedido.porcentaje(), vacioANull(pedido.categoria())));
    }

    @GetMapping("/{id}/historial")
    List<CambioJson> historial(@PathVariable UUID id) {
        return catalogo.historialDePrecios(ProductoId.de(id)).stream().map(CambioJson::de).toList();
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    /** aConsultar es Boolean (no boolean) para que, si no viene en el JSON, sea null y no un error. */
    record PrecioNuevo(Long precio, Boolean aConsultar) {
    }

    record Aumento(BigDecimal porcentaje, String categoria) {
    }

    record CategoriaJson(String slug, String nombre) {
    }

    record PrecioJson(UUID id, String slug, String nombre, CategoriaJson categoria, Long precio, boolean publicado,
                      boolean esComponente) {
        static PrecioJson de(DatosProducto p) {
            return new PrecioJson(p.id().valor(), p.slug(), p.nombre(),
                    new CategoriaJson(p.categoria().slug(), p.categoria().nombre()),
                    p.precio(), p.publicado(), p.esComponente());
        }
    }

    record PrecioPropuestoJson(UUID id, String nombre, String categoria, long precioActual, long precioNuevo) {
        static PrecioPropuestoJson de(PrecioPropuesto p) {
            return new PrecioPropuestoJson(p.id().valor(), p.nombre(), p.categoria(), p.precioActual(), p.precioNuevo());
        }
    }

    record VistaPreviaJson(BigDecimal porcentaje, String categoria, List<PrecioPropuestoJson> precios) {
        static VistaPreviaJson de(VistaPreviaDeAumento v) {
            return new VistaPreviaJson(v.porcentaje(), v.categoria(), v.precios().stream().map(PrecioPropuestoJson::de).toList());
        }
    }

    record CambioJson(Long precioAnterior, Long precioNuevo, String motivo, BigDecimal porcentaje, String usuario,
                      Instant cambiadoEn) {
        static CambioJson de(DatosCambioDePrecio c) {
            return new CambioJson(c.precioAnterior(), c.precioNuevo(), c.motivo().name(), c.porcentaje(), c.usuario(),
                    c.cambiadoEn());
        }
    }
}
