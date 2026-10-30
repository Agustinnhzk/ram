package com.ram.instalaciones.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Consultas JPQL (pasan por el filtro de taller); por eso no se usa findById. */
interface InstalacionJpaRepository extends JpaRepository<InstalacionJpaEntity, UUID> {

    @Query("select i from InstalacionJpaEntity i where i.id = :id")
    Optional<InstalacionJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select i from InstalacionJpaEntity i where i.estado = com.ram.instalaciones.EstadoInstalacion.TERMINADA order by i.numero desc")
    List<InstalacionJpaEntity> terminadas();

    @Query("select i from InstalacionJpaEntity i where i.estado <> com.ram.instalaciones.EstadoInstalacion.TERMINADA order by i.numero desc")
    List<InstalacionJpaEntity> enCurso();

    @Query("select coalesce(max(i.numero), 0) from InstalacionJpaEntity i")
    int ultimoNumero();
}

interface AbonadoJpaRepository extends JpaRepository<AbonadoJpaEntity, UUID> {

    @Query("select a from AbonadoJpaEntity a where a.id = :id")
    Optional<AbonadoJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select a from AbonadoJpaEntity a")
    List<AbonadoJpaEntity> todos();
}

interface PagoAbonoJpaRepository extends JpaRepository<PagoAbonoJpaEntity, UUID> {

    @Query("select p from PagoAbonoJpaEntity p where p.anio = :anio")
    List<PagoAbonoJpaEntity> delAnio(@Param("anio") int anio);

    @Query("select p from PagoAbonoJpaEntity p where p.abonadoId = :abonado and p.anio = :anio and p.mes = :mes")
    Optional<PagoAbonoJpaEntity> buscar(@Param("abonado") UUID abonado, @Param("anio") int anio, @Param("mes") int mes);
}
