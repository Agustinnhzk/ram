package com.ram.usuarios.adaptadores.persistencia;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.aplicacion.PersonalRepositorio;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.Personal;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PersonalRepositorioJpa implements PersonalRepositorio {

    private final PersonalJpaRepository jpa;

    PersonalRepositorioJpa(PersonalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Personal p) {
        PersonalJpaEntity entidad = jpa.buscarPorId(p.id().valor())
                .orElseGet(() -> new PersonalJpaEntity(p.id().valor()));
        entidad.actualizar(p.nombre(), p.correo().valor(), p.claveCifrada(), p.rol(), p.estaActivo());
        jpa.save(entidad);
    }

    @Override
    public Optional<Personal> buscarPorId(UsuarioId id) {
        return jpa.buscarPorId(id.valor()).map(PersonalRepositorioJpa::aDominio);
    }

    @Override
    public Optional<Personal> buscarPorCorreo(CorreoDeAcceso correo) {
        return jpa.findByCorreo(correo.valor()).map(PersonalRepositorioJpa::aDominio);
    }

    @Override
    public boolean existeCorreo(CorreoDeAcceso correo) {
        return jpa.existsByCorreo(correo.valor());
    }

    @Override
    public boolean hayAlguno() {
        return jpa.hayAlguno();
    }

    private static Personal aDominio(PersonalJpaEntity e) {
        return Personal.reconstruir(UsuarioId.de(e.getId()), e.getNombre(), new CorreoDeAcceso(e.getCorreo()),
                e.getClaveCifrada(), e.getRol(), e.isActivo());
    }
}
