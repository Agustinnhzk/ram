package com.ram.reparaciones.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface MovimientoJpaRepository extends JpaRepository<MovimientoJpaEntity, UUID> {

    /** La bitácora de varias órdenes en UNA consulta (para el listado), cada una en su orden. */
    @Query("select m from MovimientoJpaEntity m where m.ordenId in :ordenes order by m.ordenId, m.posicion")
    List<MovimientoJpaEntity> deOrdenes(@Param("ordenes") Collection<UUID> ordenes);

    @Query("select count(m) from MovimientoJpaEntity m where m.ordenId = :orden")
    int cantidadDeOrden(@Param("orden") UUID orden);
}
