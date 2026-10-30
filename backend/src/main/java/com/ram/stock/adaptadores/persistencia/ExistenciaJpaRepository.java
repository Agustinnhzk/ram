package com.ram.stock.adaptadores.persistencia;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ExistenciaJpaRepository extends JpaRepository<ExistenciaJpaEntity, UUID> {

    /**
     * SELECT ... FOR UPDATE: la fila queda bloqueada hasta el fin de la transacción.
     * Otra transacción que quiera la misma fila espera; las lecturas comunes no.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExistenciaJpaEntity e where e.productoId = :producto")
    Optional<ExistenciaJpaEntity> bloquear(@Param("producto") UUID producto);

    @Query("select e from ExistenciaJpaEntity e")
    List<ExistenciaJpaEntity> todas();

    @Query("select e from ExistenciaJpaEntity e where e.productoId in :productos")
    List<ExistenciaJpaEntity> de(@Param("productos") Collection<UUID> productos);
}
