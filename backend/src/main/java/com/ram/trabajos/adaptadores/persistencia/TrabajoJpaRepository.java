package com.ram.trabajos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TrabajoJpaRepository extends JpaRepository<TrabajoJpaEntity, UUID> {

    @Query("select t from TrabajoJpaEntity t order by t.fecha desc, t.creadoEn desc")
    List<TrabajoJpaEntity> todos();

    @Query("select t from TrabajoJpaEntity t where t.id = :id")
    Optional<TrabajoJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select t from TrabajoJpaEntity t where t.slug = :slug")
    Optional<TrabajoJpaEntity> buscarPorSlug(@Param("slug") String slug);

    @Query("select count(t) > 0 from TrabajoJpaEntity t where t.slug = :slug")
    boolean existeSlug(@Param("slug") String slug);
}
