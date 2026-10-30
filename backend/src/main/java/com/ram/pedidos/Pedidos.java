package com.ram.pedidos;

import com.ram.archivos.ArchivoSubido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de entrada del módulo. Siempre sobre el taller de la request actual. */
public interface Pedidos {

    // ─── Tienda (cliente logueado) ──────────────────────────────────────────────────────────

    Optional<DatosDePago> datosDePago();

    /** Lo que cobra el taller por armar y probar una PC de Armá tu PC. Vacío si no ofrece el armado. */
    Optional<Long> precioDelArmado();

    /** Solo el Dueño. @param precio null = el taller deja de ofrecer el armado */
    void cambiarPrecioDelArmado(Long precio);

    /**
     * Calcula el pedido con los precios actuales, sin guardar nada (para mostrarlo antes de confirmar).
     * Valida lo mismo que {@link #realizar} y tira las mismas excepciones.
     */
    Cotizacion cotizar(NuevoPedido pedido);

    /**
     * Crea el pedido con los precios ACTUALES del catálogo.
     *
     * @throws ProductoNoDisponibleException si algo no existe, no está publicado o no tiene precio
     * @throws IllegalArgumentException      si faltan datos o el armado de PC no es válido
     * @throws OperacionNoPermitidaException si el cliente ya tiene demasiados pedidos sin pagar
     */
    DetalleDePedido realizar(NuevoPedido pedido);

    List<ResumenDePedido> misPedidos();

    /** Vacío si no existe o es de otro cliente. */
    Optional<DetalleDePedido> miPedido(PedidoId id);

    /** El cliente sube el comprobante de la transferencia. */
    void informarPago(PedidoId id, ArchivoSubido comprobante);

    /** El cliente se arrepiente, mientras no haya pagado. */
    void cancelarMiPedido(PedidoId id);

    // ─── Sistema (personal) ─────────────────────────────────────────────────────────────────

    /** @param estado null = los activos (falta pagar o entregar) */
    List<ResumenDePedido> listar(EstadoPedido estado);

    /** Todos los pedidos de una cuenta de la tienda (para la ficha del cliente), del más nuevo al más viejo. */
    List<ResumenDePedido> deCuenta(UUID cuenta);

    Optional<DetalleDePedido> obtener(PedidoId id);

    /** El archivo de un comprobante. Vacío si el comprobante no es de ese pedido. */
    Optional<ArchivoDeComprobante> comprobante(PedidoId pedido, UUID comprobante);

    /** Llegó la plata. Descuenta el stock; lo que falta queda reservado (ADR-014). Dueño y Mostrador. */
    void confirmarPago(PedidoId id);

    /** El comprobante no sirve (no llegó la plata, otro monto...): vuelve a esperar el pago. Dueño y Mostrador. */
    void rechazarComprobante(PedidoId id, String motivo);

    /** Todo el personal. */
    void entregar(PedidoId id);

    /** Si ya estaba pagado, lo descontado vuelve al stock y se cancelan sus reservas. Dueño y Mostrador. */
    void cancelar(PedidoId id, String motivo);

    /**
     * Lo borra del todo, con sus comprobantes (para limpiar las pruebas, ADR-016). Solo el Dueño.
     * Si estaba pagado, antes devuelve el stock como si se cancelara.
     */
    void borrar(PedidoId id);
}
