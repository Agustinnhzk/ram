package com.ram.usuarios.adaptadores.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Datos del primer usuario del taller, para poder entrar la primera vez.
 *
 * <pre>
 * ram:
 *   dueno-inicial:
 *     taller: ram            # slug del taller
 *     nombre: Agustín
 *     correo: tu@correo.com
 *     clave: una-clave-larga
 * </pre>
 *
 * Nunca en el application.yml del repositorio: van en backend/config/application.yml,
 * que está en el .gitignore. Si no hay correo configurado, no se hace nada.
 */
@ConfigurationProperties("ram.dueno-inicial")
record DuenoInicialProperties(
        @DefaultValue("ram") String taller,
        @DefaultValue("Dueño") String nombre,
        String correo,
        String clave) {

    boolean configurado() {
        return correo != null && !correo.isBlank();
    }
}
