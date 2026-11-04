package com.ram.pedidos.adaptadores.config;

import com.ram.catalogo.Catalogo;
import com.ram.clientes.Clientes;
import com.ram.pedidos.Pedidos;
import com.ram.pedidos.aplicacion.PedidosRepositorio;
import com.ram.pedidos.aplicacion.ServicioPedidos;
import com.ram.stock.Stock;
import com.ram.usuarios.SesionActual;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
class PedidosConfig {

    @Bean
    Pedidos pedidos(PedidosRepositorio repositorio, Catalogo catalogo, Stock stock, Clientes clientes, SesionActual sesion,
                    ApplicationEventPublisher publicador, PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new PedidosTransaccional(
                new ServicioPedidos(repositorio, catalogo, stock, clientes, sesion, new PublicadorSpring(publicador), Clock.systemUTC()),
                escritura, lectura);
    }
}
