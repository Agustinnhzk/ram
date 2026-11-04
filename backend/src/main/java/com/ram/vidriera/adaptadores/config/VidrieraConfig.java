package com.ram.vidriera.adaptadores.config;

import com.ram.usuarios.SesionActual;
import com.ram.vidriera.Vidriera;
import com.ram.vidriera.aplicacion.BannersRepositorio;
import com.ram.vidriera.aplicacion.ServicioVidriera;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class VidrieraConfig {

    @Bean
    Vidriera vidriera(BannersRepositorio banners, SesionActual sesion, PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new VidrieraTransaccional(new ServicioVidriera(banners, sesion, Clock.systemUTC(),
                ZoneId.of("America/Argentina/Buenos_Aires")), escritura, lectura);
    }
}
