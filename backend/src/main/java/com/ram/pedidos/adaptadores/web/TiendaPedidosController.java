package com.ram.pedidos.adaptadores.web;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.catalogo.TipoComponente;
import com.ram.pedidos.Cotizacion;
import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.FormaDeEntrega;
import com.ram.pedidos.ItemPedido;
import com.ram.pedidos.NuevoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.PedidoNoEncontradoException;
import com.ram.pedidos.Pedidos;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Los pedidos desde la tienda. Todo exige un cliente logueado (/api/tienda/**) y token CSRF en lo que modifica.
 *
 * <pre>
 * GET  /api/tienda/datos-de-pago                    alias, titular y entidad para transferir
 * POST /api/tienda/pedidos/cotizacion               cuánto sale, sin guardar (lo que muestra el checkout)
 * POST /api/tienda/pedidos                          crea el pedido → 201 con el detalle
 * GET  /api/tienda/pedidos                          mis pedidos
 * GET  /api/tienda/pedidos/{id}                     uno mío (404 si no existe o es de otro)
 * POST /api/tienda/pedidos/{id}/comprobante         multipart "archivo" (JPG, PNG o PDF, hasta 5 MB) → 204
 * POST /api/tienda/pedidos/{id}/cancelar            mientras espera el pago → 204
 * </pre>
 *
 * Cuerpo de POST /api/tienda/pedidos:
 * <pre>
 * {
 *   "items": [
 *     {"tipo": "producto", "slug": "kit-4-camaras-dvr", "cantidad": 1},
 *     {"tipo": "pc", "piezas": {"cpu": "ryzen-5-5600", "mother": "a520m", ...}, "sinPlacaDeVideo": false,
 *      "conArmado": true, "cantidad": 1}
 *   ],
 *   "entrega": "RETIRO" | "ENVIO_ROSARIO" | "ENVIO_OTRA_LOCALIDAD", "direccion": "...", "telefono": "341 555-1234", "nota": "..."
 * }
 * </pre>
 * No hay precios en el pedido: los pone el servidor.
 */
@RestController
@RequestMapping("/api/tienda")
class TiendaPedidosController {

    private static final java.util.Set<TipoDeArchivo> TIPOS = EnumSet.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG, TipoDeArchivo.PDF);

    private final Pedidos pedidos;

    TiendaPedidosController(Pedidos pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping("/datos-de-pago")
    ResponseEntity<DatosDePago> datosDePago() {
        return ResponseEntity.of(pedidos.datosDePago());
    }

    @PostMapping("/pedidos/cotizacion")
    Cotizacion cotizar(@RequestBody PedidoNuevoJson cuerpo) {
        return pedidos.cotizar(cuerpo.aNuevoPedido());
    }

    @PostMapping("/pedidos")
    ResponseEntity<PedidoJson.Detalle> realizar(@RequestBody PedidoNuevoJson cuerpo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoJson.Detalle.paraCliente(pedidos.realizar(cuerpo.aNuevoPedido())));
    }

    @GetMapping("/pedidos")
    List<PedidoJson.Resumen> misPedidos() {
        return pedidos.misPedidos().stream().map(PedidoJson.Resumen::de).toList();
    }

    @GetMapping("/pedidos/{id}")
    PedidoJson.Detalle miPedido(@PathVariable UUID id) {
        PedidoId pedido = PedidoId.de(id);
        return pedidos.miPedido(pedido).map(PedidoJson.Detalle::paraCliente)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedido));
    }

    @PostMapping(value = "/pedidos/{id}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> subirComprobante(@PathVariable UUID id, @RequestPart(required = false) MultipartFile archivo)
            throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Elegí el archivo del comprobante");
        }
        ArchivoSubido revisado = ArchivoSubido.revisar(archivo.getOriginalFilename(), archivo.getBytes(), TIPOS);
        pedidos.informarPago(PedidoId.de(id), revisado);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pedidos/{id}/cancelar")
    ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        pedidos.cancelarMiPedido(PedidoId.de(id));
        return ResponseEntity.noContent().build();
    }

    record PedidoNuevoJson(List<ItemJson> items, FormaDeEntrega entrega, String direccion, String telefono, String nota) {

        NuevoPedido aNuevoPedido() {
            return new NuevoPedido(items == null ? List.of() : items.stream().map(ItemJson::aItem).toList(),
                    entrega, direccion, telefono, nota);
        }
    }

    /** Un ítem del carrito. "tipo" decide qué campos se usan. */
    /** conArmado: si no viene, se arma (es lo que se ofrecía antes de que el armado fuera opcional). */
    record ItemJson(String tipo, String slug, Map<String, String> piezas, Boolean sinPlacaDeVideo, Boolean conArmado,
                    Integer cantidad) {

        ItemPedido aItem() {
            int unidades = cantidad == null ? 1 : cantidad;
            return switch (tipo == null ? "" : tipo) {
                case "producto" -> new ItemPedido.Producto(slug, unidades);
                case "pc" -> new ItemPedido.PcArmada(piezasPorTipo(), Boolean.TRUE.equals(sinPlacaDeVideo),
                        !Boolean.FALSE.equals(conArmado), unidades);
                default -> throw new IllegalArgumentException("Hay un producto del carrito que no se entiende. Vaciá el carrito y volvé a cargarlo.");
            };
        }

        /** {"cpu": "ryzen-5-5600"} → {CPU: "ryzen-5-5600"}. Una clave desconocida es un error, no se ignora. */
        private Map<TipoComponente, String> piezasPorTipo() {
            Map<TipoComponente, String> resultado = new EnumMap<>(TipoComponente.class);
            if (piezas != null) {
                for (Map.Entry<String, String> pieza : piezas.entrySet()) {
                    try {
                        resultado.put(TipoComponente.valueOf(pieza.getKey().toUpperCase(Locale.ROOT)), pieza.getValue());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Hay una pieza de la PC que no se entiende. Volvé a armarla.");
                    }
                }
            }
            return resultado;
        }
    }
}
