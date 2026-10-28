package com.ram;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Levanta un PostgreSQL real en Docker para los tests de integración.
 *
 * <p>Con {@code @ServiceConnection}, Spring Boot conecta la aplicación a ese contenedor
 * solo: no hace falta configurar URL, usuario ni contraseña.
 *
 * <p>Es público porque lo importan tests de distintos paquetes (tenants, clientes).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Misma versión que compose.yaml: los tests corren contra la misma base que usás en desarrollo.
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
    }
}
