package com.ram.usuarios.adaptadores.config;

import com.ram.usuarios.aplicacion.AvisosDeCuenta;
import com.ram.usuarios.aplicacion.CifradorDeClaves;
import com.ram.usuarios.aplicacion.CuentasClientesRepositorio;
import com.ram.usuarios.aplicacion.RecuperacionesRepositorio;
import com.ram.usuarios.aplicacion.ServicioDeRecuperacion;
import com.ram.usuarios.aplicacion.SesionesDeUsuario;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/** Como AccesoTransaccional: cada caso de uso en su transacción. */
class RecuperacionTransaccional extends ServicioDeRecuperacion {

    private final TransactionTemplate escritura;

    RecuperacionTransaccional(CuentasClientesRepositorio cuentas, RecuperacionesRepositorio recuperaciones,
                              CifradorDeClaves cifrador, AvisosDeCuenta avisos, SesionesDeUsuario sesiones,
                              PlatformTransactionManager transacciones) {
        super(cuentas, recuperaciones, cifrador, avisos, sesiones, Clock.systemUTC());
        this.escritura = new TransactionTemplate(transacciones);
    }

    @Override
    public void solicitar(String correo, String direccionDeLaTienda) {
        escritura.executeWithoutResult(estado -> super.solicitar(correo, direccionDeLaTienda));
    }

    @Override
    public void restablecer(String token, String clave) {
        escritura.executeWithoutResult(estado -> super.restablecer(token, clave));
    }
}
