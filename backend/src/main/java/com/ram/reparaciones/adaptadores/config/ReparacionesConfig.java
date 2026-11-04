package com.ram.reparaciones.adaptadores.config;

import com.ram.clientes.Clientes;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.adaptadores.web.LimitadorDeIntentos;
import com.ram.reparaciones.aplicacion.OrdenesRepositorio;
import com.ram.reparaciones.aplicacion.ServicioReparaciones;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class ReparacionesConfig {

    /**
     * La zona horaria decide en qué FECHA vence una garantía. Hoy todos los talleres están en Argentina;
     * cuando haya talleres de otros lugares, pasa a ser un dato de cada taller.
     */
    static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    Reparaciones reparaciones(OrdenesRepositorio ordenes, Clientes clientes, SesionActual sesion,
                              PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new ReparacionesTransaccional(
                new ServicioReparaciones(ordenes, clientes, sesion, Clock.systemUTC(), ARGENTINA), escritura, lectura);
    }

    @Bean
    LimitadorDeIntentos limitadorDeIntentos() {
        return new LimitadorDeIntentos(Clock.systemUTC());
    }
}
