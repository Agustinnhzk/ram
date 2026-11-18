package com.ram.trabajos.adaptadores.config;

import com.ram.trabajos.Trabajos;
import com.ram.trabajos.aplicacion.ServicioTrabajos;
import com.ram.trabajos.aplicacion.TrabajosRepositorio;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class TrabajosConfig {

    @Bean
    Trabajos trabajos(TrabajosRepositorio repositorio, SesionActual sesion, PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new TrabajosTransaccional(new ServicioTrabajos(repositorio, sesion, Clock.systemUTC(),
                ZoneId.of("America/Argentina/Buenos_Aires")), escritura, lectura);
    }
}
