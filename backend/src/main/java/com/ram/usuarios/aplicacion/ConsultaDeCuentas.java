package com.ram.usuarios.aplicacion;

import com.ram.usuarios.CuentaDeTienda;
import com.ram.usuarios.CuentasDeTienda;
import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Implementa {@link CuentasDeTienda} con el repositorio de cuentas. */
public class ConsultaDeCuentas implements CuentasDeTienda {

    private final CuentasClientesRepositorio cuentas;
    private final SesionesDeUsuario sesiones;

    public ConsultaDeCuentas(CuentasClientesRepositorio cuentas, SesionesDeUsuario sesiones) {
        this.cuentas = Objects.requireNonNull(cuentas);
        this.sesiones = Objects.requireNonNull(sesiones);
    }

    @Override
    public void borrar(UUID id) {
        cuentas.borrar(UsuarioId.de(id));
        sesiones.cerrarTodas(UsuarioId.de(id));
    }

    @Override
    public Optional<CuentaDeTienda> porId(UUID id) {
        return id == null ? Optional.empty() : cuentas.buscarPorId(UsuarioId.de(id)).map(ConsultaDeCuentas::aDatos);
    }

    @Override
    public Optional<CuentaDeTienda> porCorreo(String correo) {
        return CorreoDeAcceso.intentar(correo).flatMap(cuentas::buscarPorCorreo).map(ConsultaDeCuentas::aDatos);
    }

    private static CuentaDeTienda aDatos(CuentaCliente c) {
        return new CuentaDeTienda(c.id().valor(), c.nombre(), c.correo().valor());
    }
}
