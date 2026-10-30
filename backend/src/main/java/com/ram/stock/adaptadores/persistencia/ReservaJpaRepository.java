package com.ram.stock.adaptadores.persistencia;

import com.ram.stock.EstadoDeReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ReservaJpaRepository extends JpaRepository<ReservaJpaEntity, UUID> {

    @Query("select r from ReservaJpaEntity r where r.id = :id")
    Optional<ReservaJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select r from ReservaJpaEntity r where r.estado = :estado order by r.creadaEn")
    List<ReservaJpaEntity> conEstado(@Param("estado") EstadoDeReserva estado);

    @Query("select r from ReservaJpaEntity r where r.pedidoId = :pedido order by r.creadaEn")
    List<ReservaJpaEntity> dePedido(@Param("pedido") UUID pedido);
}
