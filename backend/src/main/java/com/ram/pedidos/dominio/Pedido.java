package com.ram.pedidos.dominio;

import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.OperacionNoPermitidaException;
import com.ram.pedidos.PedidoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Un pedido de la tienda. Las reglas de estados viven acá (ver {@link EstadoPedido}): desde afuera solo se
 * puede pedir "informar el pago", "confirmarlo", etc., y el pedido decide si se puede.
 */
public final class Pedido {

    /** Para que nadie suba comprobantes sin fin a un mismo pedido. */
    public static final int COMPROBANTES_MAXIMOS = 5;
    public static final int LINEAS_MAXIMAS = 30;

    private final PedidoId id;
    private final int numero;
    private final UUID cuentaCliente;
    private final String clienteNombre;
    private final String telefono;
    private final Entrega entrega;
    private final String nota;
    private final List<Linea> lineas;
    private final Instant creadoEn;
    private final List<Comprobante> comprobantes;
    private EstadoPedido estado;
    private Instant pagadoEn;
    private Instant entregadoEn;
    private Instant canceladoEn;
    private String motivoCancelacion;

    private Pedido(PedidoId id, int numero, UUID cuentaCliente, String clienteNombre, String telefono, Entrega entrega,
                   String nota, List<Linea> lineas, Instant creadoEn, List<Comprobante> comprobantes, EstadoPedido estado,
                   Instant pagadoEn, Instant entregadoEn, Instant canceladoEn, String motivoCancelacion) {
        this.id = Objects.requireNonNull(id);
        this.numero = numero;
        this.cuentaCliente = Objects.requireNonNull(cuentaCliente);
        this.clienteNombre = Objects.requireNonNull(clienteNombre);
        this.telefono = Objects.requireNonNull(telefono);
        this.entrega = Objects.requireNonNull(entrega);
        this.nota = nota;
        this.lineas = List.copyOf(lineas);
        this.creadoEn = Objects.requireNonNull(creadoEn);
        this.comprobantes = new ArrayList<>(comprobantes);
        this.estado = Objects.requireNonNull(estado);
        this.pagadoEn = pagadoEn;
        this.entregadoEn = entregadoEn;
        this.canceladoEn = canceladoEn;
        this.motivoCancelacion = motivoCancelacion;
    }

    /** Un pedido nuevo: queda esperando el pago. */
    public static Pedido crear(int numero, UUID cuentaCliente, String clienteNombre, String telefono, Entrega entrega,
                               String nota, List<Linea> lineas, Instant ahora) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de pedido tiene que ser positivo");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }
        if (lineas.size() > LINEAS_MAXIMAS) {
            throw new IllegalArgumentException("Un pedido no puede tener más de " + LINEAS_MAXIMAS + " productos distintos");
        }
        return new Pedido(PedidoId.nuevo(), numero, cuentaCliente,
                Texto.obligatorio(clienteNombre, "el nombre", 120), Texto.telefono(telefono), entrega,
                Texto.opcional(nota, "la nota", 500), lineas, ahora, List.of(), EstadoPedido.ESPERANDO_PAGO,
                null, null, null, null);
    }

    /** Para el repositorio: arma un pedido ya guardado, sin volver a validar. */
    public static Pedido reconstruir(PedidoId id, int numero, UUID cuentaCliente, String clienteNombre, String telefono,
                                     Entrega entrega, String nota, List<Linea> lineas, Instant creadoEn,
                                     List<Comprobante> comprobantes, EstadoPedido estado, Instant pagadoEn,
                                     Instant entregadoEn, Instant canceladoEn, String motivoCancelacion) {
        return new Pedido(id, numero, cuentaCliente, clienteNombre, telefono, entrega, nota, lineas, creadoEn,
                comprobantes, estado, pagadoEn, entregadoEn, canceladoEn, motivoCancelacion);
    }

    // ─── Lo que hace el cliente ───────────────────────────────────────────────────────────────

    /** Subió el comprobante de la transferencia: el taller tiene que revisarlo. */
    public void informarPago(Comprobante comprobante) {
        if (!puedeSubirComprobante()) {
            throw new OperacionNoPermitidaException(estado == EstadoPedido.ESPERANDO_PAGO
                    ? "Ya subiste demasiados comprobantes para este pedido. Escribinos por WhatsApp."
                    : "Este pedido no está esperando un pago.");
        }
        comprobantes.add(Objects.requireNonNull(comprobante));
        estado = EstadoPedido.PAGO_INFORMADO;
    }

    public boolean puedeSubirComprobante() {
        return estado == EstadoPedido.ESPERANDO_PAGO && comprobantes.size() < COMPROBANTES_MAXIMOS;
    }

    /** El cliente puede cancelar solo mientras no informó un pago: después, lo habla con el taller. */
    public boolean puedeCancelarElCliente() {
        return estado == EstadoPedido.ESPERANDO_PAGO;
    }

    public void cancelarPorElCliente(Instant ahora) {
        if (!puedeCancelarElCliente()) {
            throw new OperacionNoPermitidaException("Este pedido ya no se puede cancelar desde la tienda. Escribinos por WhatsApp.");
        }
        cancelar("Cancelado por el cliente", ahora);
    }

    // ─── Lo que hace el taller ────────────────────────────────────────────────────────────────

    /** Llegó la plata. También desde ESPERANDO_PAGO: pagó en efectivo al retirar, o transfirió sin subir nada. */
    public void confirmarPago(Instant ahora) {
        if (estado != EstadoPedido.ESPERANDO_PAGO && estado != EstadoPedido.PAGO_INFORMADO) {
            throw new OperacionNoPermitidaException("Solo se puede confirmar el pago de un pedido que lo está esperando");
        }
        estado = EstadoPedido.PAGADO;
        pagadoEn = ahora;
    }

    /** El comprobante no sirve: se marca rechazado y el pedido vuelve a esperar el pago (el cliente puede subir otro). */
    public void rechazarComprobante(String motivo) {
        if (estado != EstadoPedido.PAGO_INFORMADO) {
            throw new OperacionNoPermitidaException("No hay un comprobante para revisar en este pedido");
        }
        String limpio = Texto.obligatorio(motivo, "el motivo del rechazo", 300);
        comprobantes.getLast().rechazar(limpio);
        estado = EstadoPedido.ESPERANDO_PAGO;
    }

    public void entregar(Instant ahora) {
        if (estado != EstadoPedido.PAGADO) {
            throw new OperacionNoPermitidaException("Solo se puede entregar un pedido pagado");
        }
        estado = EstadoPedido.ENTREGADO;
        entregadoEn = ahora;
    }

    /** El taller cancela (sin stock, el cliente no aparece...). Si ya estaba pagado, la devolución se hace aparte. */
    public void cancelar(String motivo, Instant ahora) {
        if (estado.terminado()) {
            throw new OperacionNoPermitidaException("El pedido ya está " + (estado == EstadoPedido.ENTREGADO ? "entregado" : "cancelado"));
        }
        motivoCancelacion = Texto.obligatorio(motivo, "el motivo de la cancelación", 300);
        estado = EstadoPedido.CANCELADO;
        canceladoEn = ahora;
    }

    // ─── Lectura ──────────────────────────────────────────────────────────────────────────────

    public boolean esDe(UUID cuenta) {
        return cuentaCliente.equals(cuenta);
    }

    /**
     * Cuántas unidades de cada producto lleva el pedido, sumando todas las líneas (una PC cuenta cada componente).
     * Es lo que se descuenta del stock al confirmar el pago (ADR-014).
     */
    public Map<UUID, Integer> unidades() {
        Map<UUID, Integer> unidades = new LinkedHashMap<>();
        for (Linea l : lineas) {
            for (UUID producto : l.productos()) {
                unidades.merge(producto, l.cantidad(), Integer::sum);
            }
        }
        return unidades;
    }

    public boolean tieneReservas() {
        return lineas.stream().anyMatch(Linea::sinStock);
    }

    public long total() {
        return lineas.stream().mapToLong(Linea::subtotal).reduce(0, Math::addExact);
    }

    public PedidoId id() {
        return id;
    }

    public int numero() {
        return numero;
    }

    public UUID cuentaCliente() {
        return cuentaCliente;
    }

    public String clienteNombre() {
        return clienteNombre;
    }

    public String telefono() {
        return telefono;
    }

    public Entrega entrega() {
        return entrega;
    }

    public String nota() {
        return nota;
    }

    public List<Linea> lineas() {
        return lineas;
    }

    public Instant creadoEn() {
        return creadoEn;
    }

    public List<Comprobante> comprobantes() {
        return List.copyOf(comprobantes);
    }

    public EstadoPedido estado() {
        return estado;
    }

    public Instant pagadoEn() {
        return pagadoEn;
    }

    public Instant entregadoEn() {
        return entregadoEn;
    }

    public Instant canceladoEn() {
        return canceladoEn;
    }

    public String motivoCancelacion() {
        return motivoCancelacion;
    }
}
