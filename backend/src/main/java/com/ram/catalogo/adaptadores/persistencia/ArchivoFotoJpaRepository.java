package com.ram.catalogo.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface ArchivoFotoJpaRepository extends JpaRepository<ArchivoFotoJpaEntity, UUID> {

    @Query("select a from ArchivoFotoJpaEntity a where a.fotoId = :id")
    Optional<ArchivoFotoJpaEntity> buscarPorId(@Param("id") UUID id);
}
