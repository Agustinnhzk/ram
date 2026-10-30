package com.ram.clientes.adaptadores.config;

import com.ram.clientes.Clientes;
import com.ram.clientes.aplicacion.ClientesRepositorio;
import com.ram.clientes.aplicacion.ServicioClientes;
import com.ram.usuarios.CuentasDeTienda;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
class ClientesConfig {

    /** Lo que reciben los demás módulos al pedir {@link Clientes}: el servicio, ya con transacciones. */
    @Bean
    Clientes clientes(ClientesRepositorio repositorio, CuentasDeTienda cuentas, SesionActual sesion,
                      PlatformTransactionManager transacciones) {
        TransactionTemplate escritura = new TransactionTemplate(transacciones);
        TransactionTemplate lectura = new TransactionTemplate(transacciones);
        lectura.setReadOnly(true);
        return new ClientesTransaccionales(new ServicioClientes(repositorio, cuentas, sesion), escritura, lectura);
    }
}
