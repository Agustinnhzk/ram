package com.ram.pedidos.adaptadores.correo;

import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAlTaller;
import com.ram.pedidos.PagoInformado;
import com.ram.pedidos.PedidoRealizado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Le avisa al taller por mail cuando entra un pedido o un comprobante.
 *
 * <p>{@code @ApplicationModuleListener} = se ejecuta DESPUÉS de que el pedido quedó guardado, en otro hilo.
 * El cliente no espera al mail, y si el mail falla el pedido no se pierde: el evento queda pendiente en
 * event_publication y se puede reintentar. Si el pedido falla, el mail no sale.
 */
@Component
class AvisosDePedidos {

    private final Correos correos;

    AvisosDePedidos(Correos correos) {
        this.correos = correos;
    }

    @ApplicationModuleListener
    void pedidoRealizado(PedidoRealizado pedido) {
        String entrega = switch (pedido.entrega()) {
            case RETIRO -> "Retira en el local";
            case ENVIO_ROSARIO -> "Envío en Rosario (gratis) a: " + pedido.direccion();
            case ENVIO_OTRA_LOCALIDAD -> "Envío FUERA de Rosario a: " + pedido.direccion()
                    + "\n           ⚠ Pasale el costo del envío por WhatsApp.";
        };
        String texto = """
                Entró el pedido %d.

                Cliente:   %s
                Teléfono:  %s
                Entrega:   %s
                Nota:      %s

                %s

                Total: %s

                Todavía no pagó: cuando suba el comprobante te llega otro mail.
                Lo ves en el sistema, en Pedidos.
                """.formatted(pedido.numero(), pedido.cliente(), pedido.telefono(), entrega,
                pedido.nota() == null ? "—" : pedido.nota(), String.join("\n", pedido.lineas()), pesos(pedido.total()));
        correos.enviarAlTaller(new MensajeAlTaller(
                "Pedido " + pedido.numero() + " — " + unaLinea(pedido.cliente()) + " — " + pesos(pedido.total()),
                texto, null, List.of()));
    }

    @ApplicationModuleListener
    void pagoInformado(PagoInformado pago) {
        String texto = """
                %s subió el comprobante del pedido %d (%s).

                Revisá que la transferencia haya llegado y confirmalo (o rechazalo) en el sistema, en Pedidos.
                Teléfono del cliente: %s
                """.formatted(pago.cliente(), pago.numero(), pesos(pago.total()), pago.telefono());
        correos.enviarAlTaller(new MensajeAlTaller(
                "Comprobante del pedido " + pago.numero() + " — revisar pago", texto, null, List.of()));
    }

    private static String pesos(long monto) {
        return "$ " + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-AR")).format(monto);
    }

    private static String unaLinea(String texto) {
        return texto.replaceAll("[\\r\\n\\t]+", " ");
    }
}
