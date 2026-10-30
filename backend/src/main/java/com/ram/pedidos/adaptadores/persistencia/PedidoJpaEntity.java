package com.ram.pedidos.adaptadores.persistencia;

import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.FormaDeEntrega;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Las líneas y los comprobantes cuelgan del pedido (@OneToMany con cascade): se guardan al guardar el pedido.
 * Se cargan recién cuando se usan (LAZY, lo normal en @OneToMany): el listado no las toca.
 */
@Entity
@Table(name = "pedidos")
class PedidoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    @Column(nullable = false, updatable = false)
    private int numero;

    @Column(name = "cuenta_cliente_id", nullable = false, updatable = false)
    private UUID cuentaClienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 120, updatable = false)
    private String clienteNombre;

    @Column(nullable = false, length = 30, updatable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25, updatable = false)
    private FormaDeEntrega entrega;

    @Column(length = 300, updatable = false)
    private String direccion;

    @Column(length = 500, updatable = false)
    private String nota;

    @Column(nullable = false, updatable = false)
    private long total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "pagado_en")
    private Instant pagadoEn;

    @Column(name = "entregado_en")
    private Instant entregadoEn;

    @Column(name = "cancelado_en")
    private Instant canceladoEn;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    @OrderBy("posicion")
    private List<LineaJpaEntity> lineas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    @OrderBy("subidoEn")
    private List<ComprobanteJpaEntity> comprobantes = new ArrayList<>();

    protected PedidoJpaEntity() {
        // requerido por JPA
    }

    PedidoJpaEntity(UUID id, int numero, UUID cuentaClienteId, String clienteNombre, String telefono,
                    FormaDeEntrega entrega, String direccion, String nota, long total, Instant creadoEn,
                    List<LineaJpaEntity> lineas) {
        this.id = id;
        this.numero = numero;
        this.cuentaClienteId = cuentaClienteId;
        this.clienteNombre = clienteNombre;
        this.telefono = telefono;
        this.entrega = entrega;
        this.direccion = direccion;
        this.nota = nota;
        this.total = total;
        this.creadoEn = creadoEn;
        this.lineas.addAll(lineas);
    }

    void actualizarEstado(EstadoPedido estado, Instant pagadoEn, Instant entregadoEn, Instant canceladoEn,
                          String motivoCancelacion) {
        this.estado = estado;
        this.pagadoEn = pagadoEn;
        this.entregadoEn = entregadoEn;
        this.canceladoEn = canceladoEn;
        this.motivoCancelacion = motivoCancelacion;
    }

    UUID id() {
        return id;
    }

    int numero() {
        return numero;
    }

    UUID cuentaClienteId() {
        return cuentaClienteId;
    }

    String clienteNombre() {
        return clienteNombre;
    }

    String telefono() {
        return telefono;
    }

    FormaDeEntrega entrega() {
        return entrega;
    }

    String direccion() {
        return direccion;
    }

    String nota() {
        return nota;
    }

    long total() {
        return total;
    }

    EstadoPedido estado() {
        return estado;
    }

    Instant creadoEn() {
        return creadoEn;
    }

    Instant pagadoEn() {
        return pagadoEn;
    }

    Instant entregadoEn() {
        return entregadoEn;
    }

    Instant canceladoEn() {
        return canceladoEn;
    }

    String motivoCancelacion() {
        return motivoCancelacion;
    }

    List<LineaJpaEntity> lineas() {
        return lineas;
    }

    List<ComprobanteJpaEntity> comprobantes() {
        return comprobantes;
    }
}
