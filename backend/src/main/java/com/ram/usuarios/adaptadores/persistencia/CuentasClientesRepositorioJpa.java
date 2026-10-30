package com.ram.usuarios.adaptadores.persistencia;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.aplicacion.CuentasClientesRepositorio;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CuentasClientesRepositorioJpa implements CuentasClientesRepositorio {

    private final CuentaClienteJpaRepository jpa;
    private final RecuperacionJpaRepository recuperaciones;

    CuentasClientesRepositorioJpa(CuentaClienteJpaRepository jpa, RecuperacionJpaRepository recuperaciones) {
        this.jpa = jpa;
        this.recuperaciones = recuperaciones;
    }

    @Override
    public void guardar(CuentaCliente c) {
        CuentaClienteJpaEntity entidad = jpa.buscarPorId(c.id().valor())
                .orElseGet(() -> new CuentaClienteJpaEntity(c.id().valor()));
        entidad.actualizar(c.nombre(), c.correo().valor(), c.claveCifrada(), c.correoVerificado());
        jpa.save(entidad);
    }

    @Override
    public Optional<CuentaCliente> buscarPorId(UsuarioId id) {
        return jpa.buscarPorId(id.valor()).map(CuentasClientesRepositorioJpa::aDominio);
    }

    @Override
    public Optional<CuentaCliente> buscarPorCorreo(CorreoDeAcceso correo) {
        return jpa.findByCorreo(correo.valor()).map(CuentasClientesRepositorioJpa::aDominio);
    }

    @Override
    public boolean existeCorreo(CorreoDeAcceso correo) {
        return jpa.existsByCorreo(correo.valor());
    }

    /** flush(): si la base rechaza el borrado (tiene pedidos), que salte acá y no al final de la transacción. */
    @Override
    public void borrar(UsuarioId id) {
        recuperaciones.deleteAll(recuperaciones.todasDe(id.valor()));
        jpa.buscarPorId(id.valor()).ifPresent(jpa::delete);
        jpa.flush();
    }

    private static CuentaCliente aDominio(CuentaClienteJpaEntity e) {
        return CuentaCliente.reconstruir(UsuarioId.de(e.getId()), e.getNombre(), new CorreoDeAcceso(e.getCorreo()),
                e.getClaveCifrada(), e.isCorreoVerificado());
    }
}
