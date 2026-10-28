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

    CuentasClientesRepositorioJpa(CuentaClienteJpaRepository jpa) {
        this.jpa = jpa;
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

    private static CuentaCliente aDominio(CuentaClienteJpaEntity e) {
        return CuentaCliente.reconstruir(UsuarioId.de(e.getId()), e.getNombre(), new CorreoDeAcceso(e.getCorreo()),
                e.getClaveCifrada(), e.isCorreoVerificado());
    }
}
