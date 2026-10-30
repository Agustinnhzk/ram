package com.ram.catalogo.adaptadores.persistencia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface CambioDePrecioJpaRepository extends JpaRepository<CambioDePrecioJpaEntity, UUID> {

    @Query("select h from CambioDePrecioJpaEntity h where h.productoId = :producto order by h.cambiadoEn desc")
    List<CambioDePrecioJpaEntity> deProducto(@Param("producto") UUID producto, Pageable pagina);
}
