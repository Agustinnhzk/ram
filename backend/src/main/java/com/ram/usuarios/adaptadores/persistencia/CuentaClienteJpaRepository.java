package com.ram.usuarios.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface CuentaClienteJpaRepository extends JpaRepository<CuentaClienteJpaEntity, UUID> {

    @Query("select c from CuentaClienteJpaEntity c where c.id = :id")
    Optional<CuentaClienteJpaEntity> buscarPorId(@Param("id") UUID id);

    Optional<CuentaClienteJpaEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
