package com.ram.trabajos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FotoTrabajoJpaRepository extends JpaRepository<FotoTrabajoJpaEntity, UUID> {

    @Query("select f from FotoTrabajoJpaEntity f where f.trabajoId = :trabajo order by f.posicion")
    List<FotoTrabajoJpaEntity> deTrabajo(@Param("trabajo") UUID trabajo);

    @Query("select f from FotoTrabajoJpaEntity f order by f.trabajoId, f.posicion")
    List<FotoTrabajoJpaEntity> todas();

    @Query("select f from FotoTrabajoJpaEntity f where f.id = :id")
    Optional<FotoTrabajoJpaEntity> buscarPorId(@Param("id") UUID id);
}
