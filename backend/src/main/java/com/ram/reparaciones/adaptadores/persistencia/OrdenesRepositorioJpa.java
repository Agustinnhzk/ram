package com.ram.reparaciones.adaptadores.persistencia;

import com.ram.clientes.ClienteId;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.aplicacion.OrdenesRepositorio;
import com.ram.reparaciones.dominio.Autor;
import com.ram.reparaciones.dominio.Equipo;
import com.ram.reparaciones.dominio.Movimiento;
import com.ram.reparaciones.dominio.OrdenDeReparacion;
import com.ram.usuarios.UsuarioId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class OrdenesRepositorioJpa implements OrdenesRepositorio {

    private final OrdenJpaRepository ordenes;
    private final MovimientoJpaRepository movimientos;

    OrdenesRepositorioJpa(OrdenJpaRepository ordenes, MovimientoJpaRepository movimientos) {
        this.ordenes = ordenes;
        this.movimientos = movimientos;
    }

    /**
     * Guarda la orden y agrega a la bitácora SOLO los movimientos nuevos. Como la bitácora solo crece,
     * los nuevos son los que están después de los que ya hay guardados (por posición).
     */
    @Override
    public void guardar(OrdenDeReparacion orden) {
        UUID id = orden.id().valor();
        OrdenJpaEntity entidad = ordenes.buscarPorId(id)
                .orElseGet(() -> new OrdenJpaEntity(id, orden.numero(), orden.cliente().valor(), orden.ingresadoEn()));
        entidad.actualizar(
                orden.equipo().descripcion(),
                orden.equipo().conCargador(),
                orden.equipo().procesador(),
                orden.password(),
                orden.problema(),
                orden.observaciones(),
                orden.estado(),
                orden.presupuestoMonto(),
                orden.presupuestoDescripcion(),
                orden.entregadoEn(),
                orden.garantiaDias(),
                orden.garantiaHasta());
        ordenes.save(entidad);

        List<Movimiento> bitacora = orden.movimientos();
        int guardados = movimientos.cantidadDeOrden(id);
        for (int posicion = guardados; posicion < bitacora.size(); posicion++) {
            Movimiento m = bitacora.get(posicion);
            movimientos.save(new MovimientoJpaEntity(m.id(), id, posicion, m.estado(), m.nota(), m.paraCliente(),
                    m.autor().id().valor(), m.autor().nombre(), m.cuando()));
        }
    }

    @Override
    public Optional<OrdenDeReparacion> porId(OrdenId id) {
        return ordenes.buscarPorId(id.valor()).map(e -> conBitacora(List.of(e)).getFirst());
    }

    @Override
    public Optional<OrdenDeReparacion> porNumero(int numero) {
        return ordenes.buscarPorNumero(numero).map(e -> conBitacora(List.of(e)).getFirst());
    }

    @Override
    public boolean existeNumero(int numero) {
        return ordenes.existeNumero(numero);
    }

    @Override
    public OptionalInt ultimoNumero() {
        Integer ultimo = ordenes.ultimoNumero();
        return ultimo == null ? OptionalInt.empty() : OptionalInt.of(ultimo);
    }

    @Override
    public List<OrdenDeReparacion> conEstados(Set<EstadoReparacion> estados, int limite) {
        return conBitacora(ordenes.conEstados(estados, PageRequest.of(0, limite)));
    }

    @Override
    public List<OrdenDeReparacion> deClientes(Collection<ClienteId> clientes, int limite) {
        return conBitacora(ordenes.deClientes(clientes.stream().map(ClienteId::valor).toList(), PageRequest.of(0, limite)));
    }

    /** Arma las órdenes con su bitácora, trayendo los movimientos de TODAS en una sola consulta. */
    private List<OrdenDeReparacion> conBitacora(List<OrdenJpaEntity> entidades) {
        if (entidades.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<MovimientoJpaEntity>> porOrden = movimientos
                .deOrdenes(entidades.stream().map(OrdenJpaEntity::getId).toList()).stream()
                .collect(Collectors.groupingBy(MovimientoJpaEntity::getOrdenId));
        return entidades.stream().map(e -> aDominio(e, porOrden.getOrDefault(e.getId(), List.of()))).toList();
    }

    private static OrdenDeReparacion aDominio(OrdenJpaEntity e, List<MovimientoJpaEntity> bitacora) {
        return new OrdenDeReparacion(
                OrdenId.de(e.getId()),
                e.getNumero(),
                ClienteId.de(e.getClienteId()),
                new Equipo(e.getEquipo(), e.isConCargador(), e.getProcesador()),
                e.getPasswordEquipo(),
                e.getProblema(),
                e.getObservaciones(),
                e.getIngresadoEn(),
                e.getEstado(),
                e.getPresupuestoMonto(),
                e.getPresupuestoDescripcion(),
                e.getEntregadoEn(),
                e.getGarantiaDias(),
                e.getGarantiaHasta(),
                bitacora.stream()
                        .map(m -> new Movimiento(m.getId(), m.getEstado(), m.getNota(), m.isVisibleParaCliente(),
                                new Autor(UsuarioId.de(m.getUsuarioId()), m.getUsuarioNombre()), m.getOcurridoEn()))
                        .toList());
    }
}
