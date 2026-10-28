package com.ram.tenants.aplicacion;

import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del puerto en memoria, solo para tests. Es la ventaja concreta
 * de la arquitectura hexagonal: el caso de uso se prueba sin base de datos ni Spring,
 * y los tests corren en milisegundos.
 */
public class TalleresEnMemoria implements TalleresRepositorio {

    private final List<Taller> talleres = new ArrayList<>();

    @Override
    public Optional<Taller> buscarPorSlug(Slug slug) {
        return talleres.stream().filter(t -> t.slug().equals(slug)).findFirst();
    }

    @Override
    public Optional<Taller> buscarPorDominioPropio(Dominio dominio) {
        return talleres.stream().filter(t -> t.dominiosPropios().contains(dominio)).findFirst();
    }

    @Override
    public Taller guardar(Taller taller) {
        talleres.remove(taller);
        talleres.add(taller);
        return taller;
    }
}
