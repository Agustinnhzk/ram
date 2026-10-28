package com.ram.tenants.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Repositorio de Spring Data. Solo lo usa el adaptador; nadie más del sistema lo ve. */
interface TallerJpaRepository extends JpaRepository<TallerJpaEntity, UUID> {

    Optional<TallerJpaEntity> findBySlug(String slug);

    @Query("select t from TallerJpaEntity t join t.dominios d where d = :dominio")
    Optional<TallerJpaEntity> findByDominioPropio(@Param("dominio") String dominio);
}
