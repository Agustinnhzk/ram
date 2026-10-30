package com.ram.vidriera.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BannerJpaRepository extends JpaRepository<BannerJpaEntity, UUID> {

    @Query("select b from BannerJpaEntity b order by b.orden")
    List<BannerJpaEntity> todos();

    @Query("select b from BannerJpaEntity b where b.id = :id")
    Optional<BannerJpaEntity> buscarPorId(@Param("id") UUID id);
}
