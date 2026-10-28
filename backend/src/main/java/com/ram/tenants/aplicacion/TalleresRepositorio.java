package com.ram.tenants.aplicacion;

import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;

import java.util.Optional;

/**
 * Puerto de salida: lo que el caso de uso necesita para guardar y buscar talleres.
 *
 * <p>Está escrito en el idioma del negocio, no en el de la base de datos.
 * La implementación con JPA vive en adaptadores; en los tests se usa una en memoria.
 */
public interface TalleresRepositorio {

    Optional<Taller> buscarPorSlug(Slug slug);

    Optional<Taller> buscarPorDominioPropio(Dominio dominio);

    Taller guardar(Taller taller);
}
