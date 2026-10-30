package com.ram.usuarios.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface RecuperacionJpaRepository extends JpaRepository<RecuperacionJpaEntity, UUID> {

    @Query("select r from RecuperacionJpaEntity r where r.id = :id")
    Optional<RecuperacionJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select r from RecuperacionJpaEntity r where r.tokenHash = :hash")
    Optional<RecuperacionJpaEntity> buscarPorHash(@Param("hash") String hash);

    @Query("select r from RecuperacionJpaEntity r where r.cuentaId = :cuenta and r.usadaEn is null")
    List<RecuperacionJpaEntity> sinUsarDe(@Param("cuenta") UUID cuenta);

    @Query("select r from RecuperacionJpaEntity r where r.cuentaId = :cuenta")
    List<RecuperacionJpaEntity> todasDe(@Param("cuenta") UUID cuenta);
}
