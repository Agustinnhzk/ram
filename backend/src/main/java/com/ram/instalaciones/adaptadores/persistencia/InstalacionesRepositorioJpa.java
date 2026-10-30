package com.ram.instalaciones.adaptadores.persistencia;

import com.ram.instalaciones.aplicacion.InstalacionesRepositorio;
import com.ram.instalaciones.dominio.Abonado;
import com.ram.instalaciones.dominio.FichaDeInstalacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class InstalacionesRepositorioJpa implements InstalacionesRepositorio {

    private final InstalacionJpaRepository instalaciones;
    private final AbonadoJpaRepository abonados;
    private final PagoAbonoJpaRepository pagos;

    InstalacionesRepositorioJpa(InstalacionJpaRepository instalaciones, AbonadoJpaRepository abonados, PagoAbonoJpaRepository pagos) {
        this.instalaciones = instalaciones;
        this.abonados = abonados;
        this.pagos = pagos;
    }

    @Override
    public void guardar(FichaDeInstalacion f) {
        InstalacionJpaEntity e = instalaciones.buscarPorId(f.id()).orElseGet(() -> {
            InstalacionJpaEntity nueva = new InstalacionJpaEntity(f.id());
            nueva.numero = f.numero();
            nueva.clienteId = f.clienteId();
            nueva.creadaEn = f.creadaEn();
            return nueva;
        });
        e.direccion = f.direccion();
        e.estado = f.estado();
        e.equipos = f.equipos();
        e.camaras = f.camaras();
        e.fecha = f.fecha();
        e.tecnico = f.tecnico();
        e.presupuesto = f.presupuesto();
        e.notas = f.notas();
        instalaciones.save(e);
    }

    @Override
    public Optional<FichaDeInstalacion> instalacion(UUID id) {
        return instalaciones.buscarPorId(id).map(InstalacionesRepositorioJpa::aDominio);
    }

    @Override
    public List<FichaDeInstalacion> instalaciones(boolean terminadas) {
        return (terminadas ? instalaciones.terminadas() : instalaciones.enCurso()).stream().map(InstalacionesRepositorioJpa::aDominio).toList();
    }

    @Override
    public int ultimoNumero() {
        return instalaciones.ultimoNumero();
    }

    @Override
    public void guardar(Abonado a) {
        AbonadoJpaEntity e = abonados.buscarPorId(a.id()).orElseGet(() -> {
            AbonadoJpaEntity nuevo = new AbonadoJpaEntity(a.id());
            nuevo.clienteId = a.clienteId();
            return nuevo;
        });
        e.direccion = a.direccion();
        e.monto = a.monto();
        e.activo = a.activo();
        abonados.save(e);
    }

    @Override
    public Optional<Abonado> abonado(UUID id) {
        return abonados.buscarPorId(id).map(InstalacionesRepositorioJpa::aDominio);
    }

    @Override
    public List<Abonado> abonados() {
        return abonados.todos().stream().map(InstalacionesRepositorioJpa::aDominio).toList();
    }

    @Override
    public Set<String> pagos(int anio) {
        return pagos.delAnio(anio).stream().map(p -> p.abonadoId + ":" + p.mes).collect(Collectors.toSet());
    }

    /** Marcar dos veces no duplica; desmarcar algo que no estaba no hace nada. */
    @Override
    public void marcarPago(UUID abonado, int anio, int mes, boolean pagado) {
        Optional<PagoAbonoJpaEntity> existente = pagos.buscar(abonado, anio, mes);
        if (pagado && existente.isEmpty()) {
            pagos.save(new PagoAbonoJpaEntity(abonado, anio, mes));
        } else if (!pagado) {
            existente.ifPresent(pagos::delete);
        }
    }

    private static FichaDeInstalacion aDominio(InstalacionJpaEntity e) {
        return new FichaDeInstalacion(e.id, e.numero, e.clienteId, e.direccion, e.estado, e.equipos, e.camaras, e.fecha,
                e.tecnico, e.presupuesto, e.notas, e.creadaEn);
    }

    private static Abonado aDominio(AbonadoJpaEntity e) {
        return new Abonado(e.id, e.clienteId, e.direccion, e.monto, e.activo);
    }
}
