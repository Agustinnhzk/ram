package com.ram.catalogo.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * "join fetch p.categoria": trae cada producto con su categoría en la MISMA consulta.
 * Sin eso, mostrar 100 productos haría 1 consulta + 100 más (una por categoría): el famoso problema N+1.
 */
interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, UUID> {

    @Query("select p from ProductoJpaEntity p join fetch p.categoria c order by c.orden, p.orden, p.nombre")
    List<ProductoJpaEntity> todos();

    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.id = :id")
    Optional<ProductoJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.slug = :slug")
    Optional<ProductoJpaEntity> buscarPorSlug(@Param("slug") String slug);

    @Query("select count(p) > 0 from ProductoJpaEntity p where p.slug = :slug")
    boolean existeSlug(@Param("slug") String slug);

    @Query("select coalesce(max(p.orden), 0) from ProductoJpaEntity p where p.categoria.id = :categoria")
    int ultimoOrden(@Param("categoria") UUID categoria);
}
