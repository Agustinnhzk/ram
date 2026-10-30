package com.ram.pedidos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface AjustesTiendaJpaRepository extends JpaRepository<AjustesTiendaJpaEntity, UUID> {

    /** Una fila por taller: el filtro de taller deja solo la del actual. */
    @Query("select a from AjustesTiendaJpaEntity a")
    Optional<AjustesTiendaJpaEntity> delTaller();
}
