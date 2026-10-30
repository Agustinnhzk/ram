package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;

import java.util.Optional;

/** Puerto de salida. Todo es del taller actual (ADR-005). */
public interface CuentasClientesRepositorio {

    void guardar(CuentaCliente cuenta);

    Optional<CuentaCliente> buscarPorId(UsuarioId id);

    Optional<CuentaCliente> buscarPorCorreo(CorreoDeAcceso correo);

    boolean existeCorreo(CorreoDeAcceso correo);

    /** Borra la cuenta y sus pedidos de recuperación de clave. Si tiene pedidos, la base no lo deja (FOREIGN KEY). */
    void borrar(UsuarioId id);
}
