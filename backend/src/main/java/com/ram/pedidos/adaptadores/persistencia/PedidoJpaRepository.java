package com.ram.pedidos.adaptadores.persistencia;

import com.ram.pedidos.EstadoPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPQL propio, como en los otros módulos: todas las consultas pasan por el filtro de taller (@TenantId). */
interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {

    @Query("select p from PedidoJpaEntity p where p.id = :id")
    Optional<PedidoJpaEntity> buscarPorId(@Param("id") UUID id);

    /** null si el taller todavía no tiene pedidos. */
    @Query("select max(p.numero) from PedidoJpaEntity p")
    Integer ultimoNumero();

    @Query("select count(p) from PedidoJpaEntity p where p.cuentaClienteId = :cuenta and p.estado = :estado")
    long contarDeCuentaEnEstado(@Param("cuenta") UUID cuenta, @Param("estado") EstadoPedido estado);

    @Query("select p from PedidoJpaEntity p where p.estado in :estados order by p.numero desc")
    List<PedidoJpaEntity> conEstados(@Param("estados") Collection<EstadoPedido> estados, Pageable pagina);

    @Query("select p from PedidoJpaEntity p where p.cuentaClienteId = :cuenta order by p.numero desc")
    List<PedidoJpaEntity> deCuenta(@Param("cuenta") UUID cuenta, Pageable pagina);
}
