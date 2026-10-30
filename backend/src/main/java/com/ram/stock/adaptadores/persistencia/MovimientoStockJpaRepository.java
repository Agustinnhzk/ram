package com.ram.stock.adaptadores.persistencia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface MovimientoStockJpaRepository extends JpaRepository<MovimientoStockJpaEntity, UUID> {

    @Query("select m from MovimientoStockJpaEntity m where m.productoId = :producto order by m.fecha desc, m.id")
    List<MovimientoStockJpaEntity> deProducto(@Param("producto") UUID producto, Pageable pagina);

    @Query("select m from MovimientoStockJpaEntity m where m.pedidoId = :pedido order by m.fecha")
    List<MovimientoStockJpaEntity> dePedido(@Param("pedido") UUID pedido);
}
