package com.ram.pedidos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface ArchivoComprobanteJpaRepository extends JpaRepository<ArchivoComprobanteJpaEntity, UUID> {

    @Query("select a from ArchivoComprobanteJpaEntity a where a.comprobanteId = :id")
    Optional<ArchivoComprobanteJpaEntity> buscarPorId(@Param("id") UUID id);
}
