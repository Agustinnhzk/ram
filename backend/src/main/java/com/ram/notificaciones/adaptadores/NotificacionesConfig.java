package com.ram.notificaciones.adaptadores;

import com.ram.notificaciones.Correos;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @EnableAsync: los avisos por mail de otros módulos se mandan "de fondo" (@ApplicationModuleListener),
 * después de guardar. Si el mail tarda o falla, el cliente no espera ni ve un error.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(CorreoProperties.class)
class NotificacionesConfig {

    /**
     * Spring Boot crea el JavaMailSender solo si hay spring.mail.host configurado.
     * ObjectProvider: "dámelo si existe". Si no existe, los mails van al log.
     */
    @Bean
    Correos correos(ObjectProvider<JavaMailSender> enviador, CorreoProperties propiedades,
                    @Value("${spring.mail.username:}") String usuarioDelCorreo) {
        JavaMailSender smtp = enviador.getIfAvailable();
        if (smtp == null || propiedades.destinoDelTaller() == null) {
            return new CorreosSoloEnElRegistro();
        }
        // Si no se configuró un remitente, se usa la cuenta que manda (lo que exige Gmail).
        String remitente = propiedades.remitente() != null ? propiedades.remitente() : usuarioDelCorreo;
        return new CorreosSmtp(smtp, new CorreoProperties(remitente, propiedades.destinoDelTaller()));
    }
}
