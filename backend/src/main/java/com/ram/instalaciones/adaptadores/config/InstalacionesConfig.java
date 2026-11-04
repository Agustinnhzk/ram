package com.ram.instalaciones.adaptadores.config;

import com.ram.clientes.Clientes;
import com.ram.instalaciones.DatosDeAbonado;
import com.ram.instalaciones.DatosDeInstalacion;
import com.ram.instalaciones.FilaDeAbonos;
import com.ram.instalaciones.Instalacion;
import com.ram.instalaciones.Instalaciones;
import com.ram.instalaciones.aplicacion.InstalacionesRepositorio;
import com.ram.instalaciones.aplicacion.ServicioInstalaciones;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
class InstalacionesConfig {

    @Bean
    Instalaciones instalaciones(InstalacionesRepositorio repositorio, Clientes clientes, SesionActual sesion,
                                PlatformTransactionManager transacciones) {
        return new Transaccional(new ServicioInstalaciones(repositorio, clientes, sesion, Clock.systemUTC()),
                new TransactionTemplate(transacciones));
    }

    /** Cada caso de uso en su transacción (crear el cliente y la instalación van juntos). */
    private record Transaccional(Instalaciones delegado, TransactionTemplate tx) implements Instalaciones {

        @Override
        public List<Instalacion> listar(boolean terminadas) {
            return tx.execute(e -> delegado.listar(terminadas));
        }

        @Override
        public Optional<Instalacion> obtener(UUID id) {
            return tx.execute(e -> delegado.obtener(id));
        }

        @Override
        public Instalacion crear(DatosDeInstalacion datos) {
            return tx.execute(e -> delegado.crear(datos));
        }

        @Override
        public void editar(UUID id, DatosDeInstalacion datos) {
            tx.executeWithoutResult(e -> delegado.editar(id, datos));
        }

        @Override
        public List<FilaDeAbonos> abonos(int anio) {
            return tx.execute(e -> delegado.abonos(anio));
        }

        @Override
        public UUID agregarAbonado(DatosDeAbonado datos) {
            return tx.execute(e -> delegado.agregarAbonado(datos));
        }

        @Override
        public void editarAbonado(UUID id, DatosDeAbonado datos) {
            tx.executeWithoutResult(e -> delegado.editarAbonado(id, datos));
        }

        @Override
        public void marcarPago(UUID abonado, int anio, int mes, boolean pagado) {
            tx.executeWithoutResult(e -> delegado.marcarPago(abonado, anio, mes, pagado));
        }
    }
}
