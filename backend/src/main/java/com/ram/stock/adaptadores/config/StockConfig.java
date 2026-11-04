package com.ram.stock.adaptadores.config;

import com.ram.catalogo.Catalogo;
import com.ram.stock.Stock;
import com.ram.stock.aplicacion.ServicioStock;
import com.ram.stock.aplicacion.StockRepositorio;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
class StockConfig {

    /**
     * Las transacciones usan la propagación por defecto (REQUIRED): cuando pedidos llama a descontarPorVenta
     * dentro de su transacción de "confirmar pago", el stock se descuenta en LA MISMA. O se confirma el pago y se
     * descuenta el stock, o ninguna de las dos cosas.
     */
    @Bean
    Stock stock(StockRepositorio repositorio, Catalogo catalogo, SesionActual sesion, PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new StockTransaccional(new ServicioStock(repositorio, catalogo, sesion, Clock.systemUTC()), escritura, lectura);
    }
}
