package com.ram.stock;

import com.ram.catalogo.ProductoId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Puerto de entrada del módulo. Siempre sobre el taller de la request actual (ADR-014). */
public interface Stock {

    // ─── Tienda (público) ─────────────────────────────────────────────────────────────────────

    /** El estado de cada producto con control de stock, por slug. Los que no tienen control no vienen. */
    List<DisponibilidadPublica> paraLaTienda();

    // ─── Pedidos (lo llama el módulo pedidos, que ya revisó quién lo pide) ─────────────────────

    /** Cuántas unidades hay de cada producto. Solo vienen los que tienen control de stock. */
    Map<ProductoId, Integer> disponibles(Collection<ProductoId> productos);

    /**
     * Se confirmó el pago de un pedido: descuenta lo que haya y lo que falta queda reservado.
     * Si ya se descontó para ese pedido, no hace nada (no descuenta dos veces).
     *
     * @param unidades cuántas de cada producto lleva el pedido (las PCs ya vienen separadas en componentes)
     */
    void descontarPorVenta(UUID pedido, int numeroDePedido, Map<ProductoId, Integer> unidades);

    /** Se canceló un pedido pagado: vuelve al stock todo lo que se le había descontado y se cancelan sus reservas. */
    void devolverPorCancelacion(UUID pedido, int numeroDePedido);

    // ─── Sistema (personal) ───────────────────────────────────────────────────────────────────

    /** Todos los productos del catálogo, con su stock (o sin control). */
    List<FilaDeStock> listar();

    /** Empieza a controlar el stock de un producto con lo que hay hoy. Dueño y Mostrador. */
    void controlar(ProductoId producto, int cantidadInicial);

    /** Llegó mercadería. Dueño y Mostrador. */
    void ingresar(ProductoId producto, int cantidad, String nota);

    /** Se contó y hay otra cantidad (rotura, error de carga...). El motivo es obligatorio. Dueño y Mostrador. */
    void ajustar(ProductoId producto, int cantidadContada, String motivo);

    /** Deja de controlar el stock (vuelve a venderse sin límite). Solo el Dueño, y sin reservas pendientes. */
    void dejarDeControlar(ProductoId producto);

    /** Los últimos movimientos del producto, del más nuevo al más viejo. */
    List<DatosMovimiento> movimientos(ProductoId producto);

    /** Las reservas que esperan mercadería, de la más vieja a la más nueva. */
    List<DatosReserva> reservasPendientes();

    /** Todas las reservas de un pedido (pendientes, cubiertas o canceladas). */
    List<DatosReserva> reservasDePedido(UUID pedido);

    /** Usa unidades del stock para la reserva (tiene que haber todas). Dueño y Mostrador. */
    void cubrirReserva(UUID reserva);
}
