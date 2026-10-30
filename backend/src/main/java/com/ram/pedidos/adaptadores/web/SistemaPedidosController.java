package com.ram.pedidos.adaptadores.web;

import com.ram.pedidos.ArchivoDeComprobante;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.PedidoNoEncontradoException;
import com.ram.pedidos.Pedidos;
import com.ram.pedidos.ResumenDePedido;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Los pedidos en el sistema de gestión (personal logueado).
 *
 * <pre>
 * GET  /api/sistema/pedidos?estado=PAGO_INFORMADO        sin estado: los activos (falta pagar o entregar)
 * GET  /api/sistema/pedidos?cuenta={id}                  todos los de una cuenta de la tienda (ficha del cliente)
 * GET  /api/sistema/pedidos/{id}
 * GET  /api/sistema/pedidos/{id}/comprobantes/{cid}      el archivo (para verlo en el navegador)
 * POST /api/sistema/pedidos/{id}/confirmar-pago          Dueño y Mostrador
 * POST /api/sistema/pedidos/{id}/rechazar-comprobante    {"motivo": "..."} — Dueño y Mostrador
 * POST /api/sistema/pedidos/{id}/entregar                todo el personal
 * POST /api/sistema/pedidos/{id}/cancelar                {"motivo": "..."} — Dueño y Mostrador
 * DELETE /api/sistema/pedidos/{id}                       borrarlo del todo (pruebas) — solo Dueño
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/pedidos")
class SistemaPedidosController {

    private final Pedidos pedidos;

    SistemaPedidosController(Pedidos pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping
    List<PedidoJson.Resumen> listar(@RequestParam(required = false) EstadoPedido estado,
                                    @RequestParam(required = false) UUID cuenta) {
        List<ResumenDePedido> encontrados = cuenta != null ? pedidos.deCuenta(cuenta) : pedidos.listar(estado);
        return encontrados.stream().map(PedidoJson.Resumen::de).toList();
    }

    @GetMapping("/{id}")
    PedidoJson.Detalle obtener(@PathVariable UUID id) {
        PedidoId pedido = PedidoId.de(id);
        return pedidos.obtener(pedido).map(PedidoJson.Detalle::paraPersonal)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedido));
    }

    /**
     * El archivo se manda con el tipo DETECTADO al subirlo (nunca con el que dijo el navegador), "inline" para que
     * el navegador lo muestre, y sin guardarlo en caché (son datos de pago). Spring Security ya agrega
     * X-Content-Type-Options: nosniff, que impide que el navegador "adivine" otro tipo.
     */
    @GetMapping("/{id}/comprobantes/{comprobante}")
    ResponseEntity<byte[]> comprobante(@PathVariable UUID id, @PathVariable UUID comprobante) {
        PedidoId pedido = PedidoId.de(id);
        ArchivoDeComprobante archivo = pedidos.comprobante(pedido, comprobante)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedido));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoDeContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(archivo.contenido());
    }

    @PostMapping("/{id}/confirmar-pago")
    ResponseEntity<Void> confirmarPago(@PathVariable UUID id) {
        pedidos.confirmarPago(PedidoId.de(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rechazar-comprobante")
    ResponseEntity<Void> rechazarComprobante(@PathVariable UUID id, @RequestBody MotivoJson cuerpo) {
        pedidos.rechazarComprobante(PedidoId.de(id), cuerpo.motivo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entregar")
    ResponseEntity<Void> entregar(@PathVariable UUID id) {
        pedidos.entregar(PedidoId.de(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancelar")
    ResponseEntity<Void> cancelar(@PathVariable UUID id, @RequestBody MotivoJson cuerpo) {
        pedidos.cancelar(PedidoId.de(id), cuerpo.motivo());
        return ResponseEntity.noContent().build();
    }

    record MotivoJson(String motivo) {
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrar(@PathVariable UUID id) {
        pedidos.borrar(PedidoId.de(id));
    }
}
