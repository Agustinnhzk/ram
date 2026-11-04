package com.ram.catalogo.adaptadores.config;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.aplicacion.FotosDeProductos;
import com.ram.catalogo.aplicacion.HistorialDePrecios;
import com.ram.catalogo.aplicacion.ProductosRepositorio;
import com.ram.catalogo.aplicacion.ServicioCatalogo;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
class CatalogoConfig {

    /**
     * El servicio, envuelto en transacciones. El reloj es UTC: las fechas se guardan en UTC
     * y cada pantalla las muestra en la hora de quien mira.
     */
    @Bean
    Catalogo catalogo(ProductosRepositorio productos, HistorialDePrecios historial, FotosDeProductos fotos,
                      SesionActual sesion, PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        // La zona decide qué día es "hoy" para las ofertas (vencen al terminar el día en Argentina).
        ServicioCatalogo servicio = new ServicioCatalogo(productos, historial, fotos, sesion, Clock.systemUTC(),
                ZoneId.of("America/Argentina/Buenos_Aires"));
        return new CatalogoTransaccional(servicio, escritura, lectura);
    }
}
