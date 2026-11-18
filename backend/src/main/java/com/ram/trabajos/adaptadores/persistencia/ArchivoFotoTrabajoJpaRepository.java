package com.ram.trabajos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface ArchivoFotoTrabajoJpaRepository extends JpaRepository<ArchivoFotoTrabajoJpaEntity, UUID> {

    @Query("select a from ArchivoFotoTrabajoJpaEntity a where a.fotoId = :id")
    Optional<ArchivoFotoTrabajoJpaEntity> buscarPorId(@Param("id") UUID id);
}
