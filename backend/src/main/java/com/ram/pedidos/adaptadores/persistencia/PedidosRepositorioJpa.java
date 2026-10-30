package com.ram.pedidos.adaptadores.persistencia;

import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.ResumenDePedido;
import com.ram.pedidos.aplicacion.PedidosRepositorio;
import com.ram.pedidos.dominio.Comprobante;
import com.ram.pedidos.dominio.Entrega;
import com.ram.pedidos.dominio.Linea;
import com.ram.pedidos.dominio.Pedido;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class PedidosRepositorioJpa implements PedidosRepositorio {

    private final PedidoJpaRepository pedidos;
    private final ArchivoComprobanteJpaRepository archivos;
    private final DatosDePagoJpaRepository datosDePago;
    private final AjustesTiendaJpaRepository ajustes;

    PedidosRepositorioJpa(PedidoJpaRepository pedidos, ArchivoComprobanteJpaRepository archivos,
                          DatosDePagoJpaRepository datosDePago, AjustesTiendaJpaRepository ajustes) {
        this.pedidos = pedidos;
        this.archivos = archivos;
        this.datosDePago = datosDePago;
        this.ajustes = ajustes;
    }

    /**
     * Nuevo: se crea con sus líneas. Existente: se actualiza el estado y los comprobantes
     * (se agregan los nuevos y se marca el rechazo de los que ya estaban). Las líneas no cambian nunca.
     */
    @Override
    public void guardar(Pedido pedido) {
        Optional<PedidoJpaEntity> existente = pedidos.buscarPorId(pedido.id().valor());
        if (existente.isEmpty()) {
            PedidoJpaEntity entidad = nueva(pedido);
            actualizar(entidad, pedido);
            pedidos.save(entidad);
            return;
        }
        // Ya está en la sesión de Hibernate ("managed"): alcanza con cambiarla. Al terminar la transacción,
        // Hibernate detecta los cambios y guarda (y con cascade, inserta los comprobantes nuevos). No hace falta save().
        actualizar(existente.get(), pedido);
    }

    private static void actualizar(PedidoJpaEntity entidad, Pedido pedido) {
        entidad.actualizarEstado(pedido.estado(), pedido.pagadoEn(), pedido.entregadoEn(), pedido.canceladoEn(),
                pedido.motivoCancelacion());

        Map<UUID, ComprobanteJpaEntity> guardados = entidad.comprobantes().stream()
                .collect(Collectors.toMap(ComprobanteJpaEntity::id, Function.identity()));
        for (Comprobante c : pedido.comprobantes()) {
            ComprobanteJpaEntity existente = guardados.get(c.id());
            if (existente == null) {
                existente = new ComprobanteJpaEntity(c.id(), c.nombre(), c.tipo(), c.tamano(), c.subidoEn());
                entidad.comprobantes().add(existente);
            }
            existente.actualizar(c.rechazado(), c.motivoRechazo());
        }
    }

    private static PedidoJpaEntity nueva(Pedido p) {
        List<LineaJpaEntity> lineas = new ArrayList<>();
        for (int i = 0; i < p.lineas().size(); i++) {
            Linea l = p.lineas().get(i);
            lineas.add(new LineaJpaEntity(i, l.descripcion(), l.detalle().toArray(String[]::new), l.referencia(),
                    l.cantidad(), l.precioUnitario(), l.productos().toArray(UUID[]::new), l.sinStock()));
        }
        return new PedidoJpaEntity(p.id().valor(), p.numero(), p.cuentaCliente(), p.clienteNombre(), p.telefono(),
                p.entrega().forma(), p.entrega().direccion(), p.nota(), p.total(), p.creadoEn(), lineas);
    }

    /** Primero los archivos (apuntan a los comprobantes); las líneas y los comprobantes se van con el pedido (cascade). */
    @Override
    public void borrar(PedidoId id) {
        pedidos.buscarPorId(id.valor()).ifPresent(entidad -> {
            entidad.comprobantes().forEach(c -> archivos.buscarPorId(c.id()).ifPresent(archivos::delete));
            archivos.flush();
            pedidos.delete(entidad);
            pedidos.flush();
        });
    }

    /**
     * flush(): el archivo apunta al comprobante (FOREIGN KEY), así que el comprobante tiene que estar
     * insertado antes. Con flush, Hibernate manda ya los INSERT pendientes del pedido.
     */
    @Override
    public void guardarArchivo(UUID comprobante, byte[] contenido) {
        pedidos.flush();
        archivos.save(new ArchivoComprobanteJpaEntity(comprobante, contenido));
    }

    @Override
    public Optional<Pedido> porId(PedidoId id) {
        return pedidos.buscarPorId(id.valor()).map(PedidosRepositorioJpa::aDominio);
    }

    @Override
    public Optional<byte[]> archivo(UUID comprobante) {
        return archivos.buscarPorId(comprobante).map(ArchivoComprobanteJpaEntity::contenido);
    }

    @Override
    public int siguienteNumero() {
        Integer ultimo = pedidos.ultimoNumero();
        return ultimo == null ? 1 : ultimo + 1;
    }

    @Override
    public int cantidadEsperandoPago(UUID cuenta) {
        return (int) pedidos.contarDeCuentaEnEstado(cuenta, EstadoPedido.ESPERANDO_PAGO);
    }

    @Override
    public List<ResumenDePedido> resumenes(Set<EstadoPedido> estados, int limite) {
        return pedidos.conEstados(estados, PageRequest.of(0, limite)).stream().map(PedidosRepositorioJpa::resumen).toList();
    }

    @Override
    public List<ResumenDePedido> resumenesDeCuenta(UUID cuenta, int limite) {
        return pedidos.deCuenta(cuenta, PageRequest.of(0, limite)).stream().map(PedidosRepositorioJpa::resumen).toList();
    }

    @Override
    public Optional<DatosDePago> datosDePago() {
        return datosDePago.delTaller().map(d -> new DatosDePago(d.alias(), d.titular(), d.entidad(), d.cbu()));
    }

    @Override
    public Optional<Long> precioDelArmado() {
        return ajustes.delTaller().map(AjustesTiendaJpaEntity::precioArmadoPc);
    }

    /** Si el taller todavía no tiene fila de ajustes (uno nuevo), se crea. */
    @Override
    public void guardarPrecioDelArmado(Long precio) {
        AjustesTiendaJpaEntity entidad = ajustes.delTaller().orElseGet(() -> new AjustesTiendaJpaEntity(UUID.randomUUID()));
        entidad.cambiarPrecioArmadoPc(precio);
        ajustes.save(entidad);
    }

    /** Solo campos del pedido: no toca líneas ni comprobantes, así el listado es UNA consulta. */
    private static ResumenDePedido resumen(PedidoJpaEntity e) {
        return new ResumenDePedido(PedidoId.de(e.id()), e.numero(), e.clienteNombre(), e.telefono(), e.entrega(),
                e.total(), e.estado(), e.creadoEn());
    }

    private static Pedido aDominio(PedidoJpaEntity e) {
        return Pedido.reconstruir(
                PedidoId.de(e.id()), e.numero(), e.cuentaClienteId(), e.clienteNombre(), e.telefono(),
                Entrega.de(e.entrega(), e.direccion()), e.nota(),
                e.lineas().stream().map(l -> new Linea(l.descripcion(), List.of(l.detalle()), l.referencia(),
                        l.cantidad(), l.precioUnitario(), List.of(l.productos()), l.sinStock())).toList(),
                e.creadoEn(),
                e.comprobantes().stream().map(c -> new Comprobante(c.id(), c.nombreArchivo(), c.tipo(), c.tamano(),
                        c.subidoEn(), c.rechazado(), c.motivoRechazo())).toList(),
                e.estado(), e.pagadoEn(), e.entregadoEn(), e.canceladoEn(), e.motivoCancelacion());
    }
}
