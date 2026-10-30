package com.ram.pedidos.dominio;

import com.ram.archivos.TipoDeArchivo;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.FormaDeEntrega;
import com.ram.pedidos.OperacionNoPermitidaException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    private static final Instant AHORA = Instant.parse("2026-10-29T15:00:00Z");
    private final UUID cuenta = UUID.randomUUID();

    private Pedido nuevo() {
        return Pedido.crear(1, cuenta, "Ana", "341 555-1234", Entrega.de(FormaDeEntrega.RETIRO, "se ignora"), null,
                List.of(new Linea("Kit", List.of(), "/productos/kit", 2, 100_000),
                        new Linea("PC", List.of("Procesador: X"), "/arma-tu-pc", 1, 500_000)), AHORA);
    }

    private static Comprobante comprobante() {
        return new Comprobante(UUID.randomUUID(), "pago.jpg", TipoDeArchivo.JPG, 1000, AHORA, false, null);
    }

    @Test
    void unPedidoNuevoEsperaElPago_yElTotalSaleDeLasLineas() {
        Pedido pedido = nuevo();

        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ESPERANDO_PAGO);
        assertThat(pedido.total()).isEqualTo(700_000L);
        assertThat(pedido.entrega().direccion()).as("con retiro no se guarda dirección").isNull();
        assertThat(pedido.esDe(cuenta)).isTrue();
        assertThat(pedido.esDe(UUID.randomUUID())).isFalse();
    }

    @Test
    void elCaminoFeliz_comprobanteConfirmadoYEntregado() {
        Pedido pedido = nuevo();
        pedido.informarPago(comprobante());
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.PAGO_INFORMADO);
        assertThat(pedido.puedeSubirComprobante()).isFalse();

        pedido.confirmarPago(AHORA);
        pedido.entregar(AHORA.plusSeconds(3600));

        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ENTREGADO);
        assertThat(pedido.pagadoEn()).isEqualTo(AHORA);
    }

    @Test
    void unComprobanteRechazadoVuelveAEsperarElPago_yPuedeSubirOtro() {
        Pedido pedido = nuevo();
        pedido.informarPago(comprobante());
        pedido.rechazarComprobante("No llegó la transferencia");

        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ESPERANDO_PAGO);
        assertThat(pedido.comprobantes().getFirst().rechazado()).isTrue();
        assertThat(pedido.comprobantes().getFirst().motivoRechazo()).isEqualTo("No llegó la transferencia");

        pedido.informarPago(comprobante());
        assertThat(pedido.comprobantes()).hasSize(2);
    }

    @Test
    void rechazarExigeMotivo() {
        Pedido pedido = nuevo();
        pedido.informarPago(comprobante());

        assertThatThrownBy(() -> pedido.rechazarComprobante(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.PAGO_INFORMADO);
    }

    @Test
    void hayUnTopeDeComprobantes() {
        Pedido pedido = nuevo();
        for (int i = 0; i < Pedido.COMPROBANTES_MAXIMOS; i++) {
            pedido.informarPago(comprobante());
            pedido.rechazarComprobante("No");
        }
        assertThatThrownBy(() -> pedido.informarPago(comprobante()))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("demasiados");
    }

    @Test
    void noSePuedeSaltearPasos() {
        Pedido pedido = nuevo();
        assertThatThrownBy(() -> pedido.entregar(AHORA)).isInstanceOf(OperacionNoPermitidaException.class);
        assertThatThrownBy(() -> pedido.rechazarComprobante("x")).isInstanceOf(OperacionNoPermitidaException.class);

        pedido.confirmarPago(AHORA);
        assertThatThrownBy(() -> pedido.informarPago(comprobante())).isInstanceOf(OperacionNoPermitidaException.class);
        assertThatThrownBy(() -> pedido.confirmarPago(AHORA)).isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void elClienteCancelaSoloMientrasNoInformoElPago() {
        Pedido pedido = nuevo();
        pedido.informarPago(comprobante());
        assertThatThrownBy(() -> pedido.cancelarPorElCliente(AHORA)).isInstanceOf(OperacionNoPermitidaException.class);

        Pedido otro = nuevo();
        otro.cancelarPorElCliente(AHORA);
        assertThat(otro.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(otro.motivoCancelacion()).isEqualTo("Cancelado por el cliente");
    }

    @Test
    void elTallerCancelaConMotivo_peroNoUnoEntregado() {
        Pedido pedido = nuevo();
        pedido.confirmarPago(AHORA);
        pedido.cancelar("Sin stock, se devolvió la plata", AHORA);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThatThrownBy(() -> pedido.cancelar("otra vez", AHORA)).isInstanceOf(OperacionNoPermitidaException.class);

        Pedido entregado = nuevo();
        entregado.confirmarPago(AHORA);
        entregado.entregar(AHORA);
        assertThatThrownBy(() -> entregado.cancelar("x", AHORA)).isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void conEnvioLaDireccionEsObligatoria() {
        assertThatThrownBy(() -> Entrega.de(FormaDeEntrega.ENVIO_ROSARIO, "  ")).hasMessageContaining("dirección");
        assertThatThrownBy(() -> Entrega.de(null, null)).hasMessageContaining("retirás");
        assertThat(Entrega.de(FormaDeEntrega.ENVIO_ROSARIO, " Córdoba 1234 ").direccion()).isEqualTo("Córdoba 1234");
        assertThatThrownBy(() -> Entrega.de(FormaDeEntrega.ENVIO_OTRA_LOCALIDAD, null)).hasMessageContaining("dirección");
    }

    @Test
    void validaLasLineas() {
        assertThatThrownBy(() -> new Linea("Kit", List.of(), "/p", 0, 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Linea("Kit", List.of(), "/p", 21, 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Linea("Kit", List.of(), "/p", 1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Pedido.crear(1, cuenta, "Ana", "3415551234", Entrega.de(FormaDeEntrega.RETIRO, null), null,
                List.of(), AHORA)).hasMessageContaining("vacío");
    }
}
