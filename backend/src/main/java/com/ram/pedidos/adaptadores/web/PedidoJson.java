package com.ram.pedidos.adaptadores.web;

import com.ram.pedidos.DatosComprobante;
import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.DetalleDePedido;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.FormaDeEntrega;
import com.ram.pedidos.LineaDePedido;
import com.ram.pedidos.ResumenDePedido;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** Lo que sale por JSON. Los ids van como texto (no como {"valor": ...}). */
final class PedidoJson {

    private PedidoJson() {
    }

    record Resumen(UUID id, int numero, String cliente, String telefono, FormaDeEntrega entrega, long total,
                   EstadoPedido estado, Instant creadoEn) {
        static Resumen de(ResumenDePedido r) {
            return new Resumen(r.id().valor(), r.numero(), r.cliente(), r.telefono(), r.entrega(), r.total(), r.estado(), r.creadoEn());
        }
    }

    /** @param url para abrir el archivo; solo en el sistema (null en la tienda) */
    record Comprobante(UUID id, String nombre, String tipoDeContenido, int tamano, Instant subidoEn, boolean rechazado,
                       String motivoRechazo, String url) {
    }

    record Detalle(UUID id, int numero, String cliente, String telefono, FormaDeEntrega entrega, String direccion, String nota,
                   List<LineaDePedido> lineas, long total, EstadoPedido estado, Instant creadoEn, Instant pagadoEn,
                   Instant entregadoEn, Instant canceladoEn, String motivoCancelacion, List<Comprobante> comprobantes,
                   DatosDePago datosDePago, boolean puedeSubirComprobante, boolean puedeCancelar) {

        /** Para la tienda: sin links a los archivos. */
        static Detalle paraCliente(DetalleDePedido d) {
            return de(d, c -> null);
        }

        static Detalle paraPersonal(DetalleDePedido d) {
            return de(d, c -> "/api/sistema/pedidos/" + d.id().valor() + "/comprobantes/" + c.id());
        }

        private static Detalle de(DetalleDePedido d, Function<DatosComprobante, String> url) {
            return new Detalle(d.id().valor(), d.numero(), d.cliente(), d.telefono(), d.entrega(), d.direccion(), d.nota(),
                    d.lineas(), d.total(), d.estado(), d.creadoEn(), d.pagadoEn(), d.entregadoEn(), d.canceladoEn(),
                    d.motivoCancelacion(),
                    d.comprobantes().stream().map(c -> new Comprobante(c.id(), c.nombre(), c.tipoDeContenido(), c.tamano(),
                            c.subidoEn(), c.rechazado(), c.motivoRechazo(), url.apply(c))).toList(),
                    d.datosDePago(), d.puedeSubirComprobante(), d.puedeCancelar());
        }
    }
}
