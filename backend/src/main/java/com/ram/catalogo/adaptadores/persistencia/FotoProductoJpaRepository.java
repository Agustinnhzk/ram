package com.ram.catalogo.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FotoProductoJpaRepository extends JpaRepository<FotoProductoJpaEntity, UUID> {

    @Query("select f from FotoProductoJpaEntity f where f.productoId = :producto order by f.posicion")
    List<FotoProductoJpaEntity> deProducto(@Param("producto") UUID producto);

    @Query("select f from FotoProductoJpaEntity f order by f.productoId, f.posicion")
    List<FotoProductoJpaEntity> todas();

    @Query("select f from FotoProductoJpaEntity f where f.id = :id")
    Optional<FotoProductoJpaEntity> buscarPorId(@Param("id") UUID id);
}
