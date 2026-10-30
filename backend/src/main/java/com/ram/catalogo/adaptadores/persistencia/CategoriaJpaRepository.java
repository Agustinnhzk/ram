package com.ram.catalogo.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/** Como en clientes: consultas JPQL propias, que siempre pasan por el filtro de taller. */
interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, UUID> {

    @Query("select c from CategoriaJpaEntity c order by c.orden, c.nombre")
    List<CategoriaJpaEntity> todas();

    @Query("select c from CategoriaJpaEntity c where c.id = :id")
    java.util.Optional<CategoriaJpaEntity> buscarPorId(@org.springframework.data.repository.query.Param("id") UUID id);

    @Query("select c from CategoriaJpaEntity c where c.slug = :slug")
    java.util.Optional<CategoriaJpaEntity> buscarPorSlug(@org.springframework.data.repository.query.Param("slug") String slug);

    /** null si el taller no tiene categorías. */
    @Query("select max(c.orden) from CategoriaJpaEntity c")
    Integer ultimoOrden();
}
