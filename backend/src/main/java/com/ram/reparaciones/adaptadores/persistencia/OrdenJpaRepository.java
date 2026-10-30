package com.ram.reparaciones.adaptadores.persistencia;

import com.ram.reparaciones.EstadoReparacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Consultas JPQL propias: todas pasan por el filtro de taller (@TenantId), como en los otros módulos. */
interface OrdenJpaRepository extends JpaRepository<OrdenJpaEntity, UUID> {

    @Query("select o from OrdenJpaEntity o where o.id = :id")
    Optional<OrdenJpaEntity> buscarPorId(@Param("id") UUID id);

    @Query("select o from OrdenJpaEntity o where o.numero = :numero")
    Optional<OrdenJpaEntity> buscarPorNumero(@Param("numero") int numero);

    @Query("select count(o) > 0 from OrdenJpaEntity o where o.numero = :numero")
    boolean existeNumero(@Param("numero") int numero);

    /** null si el taller todavía no tiene órdenes. */
    @Query("select max(o.numero) from OrdenJpaEntity o")
    Integer ultimoNumero();

    @Query("select o from OrdenJpaEntity o where o.estado in :estados order by o.numero desc")
    List<OrdenJpaEntity> conEstados(@Param("estados") Collection<EstadoReparacion> estados, Pageable pagina);

    @Query("select o from OrdenJpaEntity o where o.clienteId in :clientes order by o.numero desc")
    List<OrdenJpaEntity> deClientes(@Param("clientes") Collection<UUID> clientes, Pageable pagina);
}
