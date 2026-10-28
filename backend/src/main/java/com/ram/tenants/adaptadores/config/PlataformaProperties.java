package com.ram.tenants.adaptadores.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la plataforma, leída del application.yml:
 *
 * <pre>
 * ram:
 *   plataforma:
 *     dominio-base: localhost      # en producción, el dominio de la plataforma
 * </pre>
 */
@ConfigurationProperties("ram.plataforma")
public record PlataformaProperties(String dominioBase) {
}
