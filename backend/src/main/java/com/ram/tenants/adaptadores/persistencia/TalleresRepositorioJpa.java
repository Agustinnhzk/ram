package com.ram.tenants.adaptadores.persistencia;

import com.ram.tenants.TallerId;
import com.ram.tenants.aplicacion.TalleresRepositorio;
import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de salida: implementa el puerto {@link TalleresRepositorio} con JPA.
 * Su único trabajo es traducir entre el dominio y la tabla.
 */
@Component
class TalleresRepositorioJpa implements TalleresRepositorio {

    private final TallerJpaRepository jpa;

    TalleresRepositorioJpa(TallerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Taller> buscarPorSlug(Slug slug) {
        return jpa.findBySlug(slug.valor()).map(TalleresRepositorioJpa::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Taller> buscarPorDominioPropio(Dominio dominio) {
        return jpa.findByDominioPropio(dominio.valor()).map(TalleresRepositorioJpa::aDominio);
    }

    @Override
    @Transactional
    public Taller guardar(Taller taller) {
        return aDominio(jpa.save(aEntidad(taller)));
    }

    private static Taller aDominio(TallerJpaEntity e) {
        return Taller.reconstruir(
                TallerId.de(e.getId()),
                e.getNombre(),
                new Slug(e.getSlug()),
                e.getEstado(),
                e.getDominios().stream().map(Dominio::new).collect(Collectors.toSet()));
    }

    private static TallerJpaEntity aEntidad(Taller t) {
        return new TallerJpaEntity(
                t.id().valor(),
                t.nombre(),
                t.slug().valor(),
                t.estado(),
                t.dominiosPropios().stream().map(Dominio::valor).collect(Collectors.toSet()));
    }
}
