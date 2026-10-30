package com.ram.pedidos.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface DatosDePagoJpaRepository extends JpaRepository<DatosDePagoJpaEntity, UUID> {

    /** El filtro de taller deja solo la fila del taller actual (hay una por taller). */
    @Query("select d from DatosDePagoJpaEntity d")
    Optional<DatosDePagoJpaEntity> delTaller();
}
