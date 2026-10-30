package com.ram.vidriera.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface ImagenBannerJpaRepository extends JpaRepository<ImagenBannerJpaEntity, UUID> {

    @Query("select i from ImagenBannerJpaEntity i where i.bannerId = :id")
    Optional<ImagenBannerJpaEntity> buscarPorId(@Param("id") UUID id);
}
