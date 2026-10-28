package com.ram.clientes.adaptadores.persistencia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ninguna consulta menciona el taller: Hibernate agrega el filtro solo, porque la entidad tiene @TenantId.
 *
 * <p>Se usa una consulta propia para buscar por id (en vez de findById) a propósito:
 * las consultas JPQL siempre pasan por el filtro de taller, y así queda explícito.
 */
interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, UUID> {

    @Query("select c from ClienteJpaEntity c where c.id = :id")
    Optional<ClienteJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select c from ClienteJpaEntity c "
            + "where c.nombreBusqueda like concat('%', :texto, '%') "
            + "order by c.nombreCompleto")
    List<ClienteJpaEntity> buscarPorNombre(@Param("texto") String texto, Pageable pagina);
}
