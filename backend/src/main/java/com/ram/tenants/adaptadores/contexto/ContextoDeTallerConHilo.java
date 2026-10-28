package com.ram.tenants.adaptadores.contexto;

import com.ram.tenants.ContextoDeTaller;
import com.ram.tenants.TallerId;
import com.ram.tenants.aplicacion.TalleresRepositorio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ContextoDeTallerConHilo implements ContextoDeTaller {

    private final TalleresRepositorio talleres;

    ContextoDeTallerConHilo(TalleresRepositorio talleres) {
        this.talleres = talleres;
    }

    @Override
    public Optional<TallerId> buscarPorSlug(String slug) {
        try {
            return talleres.buscarPorSlug(Slug.de(slug)).map(Taller::id);
        } catch (IllegalArgumentException | NullPointerException slugInvalido) {
            return Optional.empty();
        }
    }

    @Override
    public void ejecutarComo(TallerId taller, Runnable accion) {
        ContextoTaller.con(taller, () -> {
            accion.run();
            return null;
        });
    }
}
