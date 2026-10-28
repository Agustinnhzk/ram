package com.ram.tenants.adaptadores.config;

import com.ram.tenants.aplicacion.ResolverTaller;
import com.ram.tenants.aplicacion.TalleresRepositorio;
import com.ram.tenants.dominio.Dominio;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Arma las piezas del módulo. Los casos de uso no tienen anotaciones de Spring:
 * es acá donde se conectan con sus dependencias reales.
 */
@Configuration
@EnableConfigurationProperties(PlataformaProperties.class)
class TenantsConfig {

    @Bean
    ResolverTaller resolverTaller(TalleresRepositorio talleres, PlataformaProperties plataforma) {
        return new ResolverTaller(talleres, Dominio.de(plataforma.dominioBase()));
    }
}
