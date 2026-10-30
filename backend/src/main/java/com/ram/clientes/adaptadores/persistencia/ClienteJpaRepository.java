package com.ram.clientes.adaptadores.persistencia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query("select c from ClienteJpaEntity c where c.id in :ids")
    List<ClienteJpaEntity> buscarVarios(@Param("ids") Collection<UUID> ids);

    @Query("select c from ClienteJpaEntity c "
            + "where c.nombreBusqueda like concat('%', :texto, '%') "
            + "order by c.nombreCompleto")
    List<ClienteJpaEntity> buscarPorNombre(@Param("texto") String texto, Pageable pagina);

    @Query("select c from ClienteJpaEntity c where c.telefono like concat('%', :digitos, '%') order by c.nombreCompleto")
    List<ClienteJpaEntity> buscarPorTelefono(@Param("digitos") String digitos, Pageable pagina);

    @Query("select c from ClienteJpaEntity c order by c.creadoEn desc, c.nombreCompleto")
    List<ClienteJpaEntity> ultimos(Pageable pagina);

    @Query("select c from ClienteJpaEntity c where c.cuentaTiendaId = :cuenta")
    Optional<ClienteJpaEntity> porCuenta(@Param("cuenta") UUID cuenta);

    @Query("select c from ClienteJpaEntity c where c.cuentaTiendaId is null and c.correo = :correo")
    List<ClienteJpaEntity> sinCuentaConCorreo(@Param("correo") String correo);

    @Query("select c from ClienteJpaEntity c where c.cuentaTiendaId is null and c.telefono = :telefono")
    List<ClienteJpaEntity> sinCuentaConTelefono(@Param("telefono") String telefono);
}
