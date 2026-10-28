package com.ram.usuarios.adaptadores.seguridad;

import com.ram.tenants.TallerActual;
import com.ram.usuarios.SesionActual;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class InterceptoresConfig implements WebMvcConfigurer {

    private final TallerActual tallerActual;
    private final SesionActual sesionActual;

    InterceptoresConfig(TallerActual tallerActual, SesionActual sesionActual) {
        this.tallerActual = tallerActual;
        this.sesionActual = sesionActual;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(new VerificacionDeTallerInterceptor(tallerActual, sesionActual))
                .addPathPatterns("/api/**");
    }
}
