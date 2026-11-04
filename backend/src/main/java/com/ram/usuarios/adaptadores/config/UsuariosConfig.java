package com.ram.usuarios.adaptadores.config;

import com.ram.usuarios.CuentasDeTienda;
import com.ram.usuarios.aplicacion.AvisosDeCuenta;
import com.ram.usuarios.aplicacion.CifradorDeClaves;
import com.ram.usuarios.aplicacion.ConsultaDeCuentas;
import com.ram.usuarios.aplicacion.CuentasClientesRepositorio;
import com.ram.usuarios.aplicacion.PersonalRepositorio;
import com.ram.usuarios.aplicacion.RecuperacionesRepositorio;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import com.ram.usuarios.aplicacion.ServicioDeRecuperacion;
import com.ram.usuarios.aplicacion.SesionesDeUsuario;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(DuenoInicialProperties.class)
class UsuariosConfig {

    @Bean
    ServicioDeAcceso servicioDeAcceso(PersonalRepositorio personal, CuentasClientesRepositorio cuentas,
                                      CifradorDeClaves cifrador, PlatformTransactionManager transacciones) {
        return new AccesoTransaccional(personal, cuentas, cifrador, transacciones);
    }

    /**
     * Para otros módulos (clientes). Sin transacción propia: se usa dentro de la de quien la llama.
     * Si alguien la llamara suelta, cada consulta abre la suya (Spring Data).
     */
    @Bean
    CuentasDeTienda cuentasDeTienda(CuentasClientesRepositorio cuentas, SesionesDeUsuario sesiones) {
        return new ConsultaDeCuentas(cuentas, sesiones);
    }

    @Bean
    ServicioDeRecuperacion servicioDeRecuperacion(CuentasClientesRepositorio cuentas, RecuperacionesRepositorio recuperaciones,
                                                  CifradorDeClaves cifrador, AvisosDeCuenta avisos, SesionesDeUsuario sesiones,
                                                  PlatformTransactionManager transacciones) {
        return new RecuperacionTransaccional(cuentas, recuperaciones, cifrador, avisos, sesiones, transacciones);
    }
}
