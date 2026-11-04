package com.ram.contactos.adaptadores.config;

import com.ram.contactos.Contactos;
import com.ram.contactos.adaptadores.web.LimitadorDeEnvios;
import com.ram.contactos.aplicacion.ServicioContactos;
import com.ram.notificaciones.Correos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Sin transacciones: el módulo no usa la base. */
@Configuration
class ContactosConfig {

    @Bean
    Contactos contactos(Correos correos) {
        return new ServicioContactos(correos);
    }

    @Bean
    LimitadorDeEnvios limitadorDeEnvios() {
        return new LimitadorDeEnvios(Clock.systemUTC());
    }
}
