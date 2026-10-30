package com.ram.usuarios.adaptadores.persistencia;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.aplicacion.RecuperacionesRepositorio;
import com.ram.usuarios.dominio.Recuperacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class RecuperacionesRepositorioJpa implements RecuperacionesRepositorio {

    private final RecuperacionJpaRepository jpa;

    RecuperacionesRepositorioJpa(RecuperacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Recuperacion r) {
        RecuperacionJpaEntity entidad = jpa.buscarPorId(r.id())
                .orElseGet(() -> new RecuperacionJpaEntity(r.id(), r.cuenta().valor(), r.tokenHash(), r.creadaEn(), r.venceEn()));
        entidad.marcarUsada(r.usadaEn());
        jpa.save(entidad);
    }

    @Override
    public Optional<Recuperacion> porHash(String tokenHash) {
        return jpa.buscarPorHash(tokenHash).map(RecuperacionesRepositorioJpa::aDominio);
    }

    @Override
    public List<Recuperacion> sinUsarDe(UsuarioId cuenta) {
        return jpa.sinUsarDe(cuenta.valor()).stream().map(RecuperacionesRepositorioJpa::aDominio).toList();
    }

    private static Recuperacion aDominio(RecuperacionJpaEntity e) {
        return Recuperacion.reconstruir(e.getId(), UsuarioId.de(e.getCuentaId()), e.getTokenHash(), e.getCreadaEn(),
                e.getVenceEn(), e.getUsadaEn());
    }
}
