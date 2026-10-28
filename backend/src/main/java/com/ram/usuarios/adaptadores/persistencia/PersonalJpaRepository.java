package com.ram.usuarios.adaptadores.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Todas las consultas pasan por el filtro de taller de Hibernate (por eso buscarPorId es JPQL y no findById). */
interface PersonalJpaRepository extends JpaRepository<PersonalJpaEntity, UUID> {

    @Query("select p from PersonalJpaEntity p where p.id = :id")
    Optional<PersonalJpaEntity> buscarPorId(@Param("id") UUID id);

    Optional<PersonalJpaEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    @Query("select count(p) > 0 from PersonalJpaEntity p")
    boolean hayAlguno();
}
