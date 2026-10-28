package com.ram.usuarios.adaptadores.seguridad;

import com.ram.tenants.TallerActual;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

/**
 * Una sesión solo vale en el taller donde se inició (ADR-002).
 *
 * <p>En producción las cookies ya son de cada dominio, así que esto casi nunca debería pasar.
 * Pero es una regla de seguridad y no la dejamos en manos del navegador: si una sesión del taller A
 * aparece en el taller B, se rechaza y se cierra.
 *
 * <p>Es un interceptor de Spring MVC porque corre después de todos los filtros:
 * cuando llega acá, ya se sabe el taller (filtro de tenants) y el usuario (Spring Security).
 */
class VerificacionDeTallerInterceptor implements HandlerInterceptor {

    private final TallerActual tallerActual;
    private final SesionActual sesionActual;

    VerificacionDeTallerInterceptor(TallerActual tallerActual, SesionActual sesionActual) {
        this.tallerActual = tallerActual;
        this.sesionActual = sesionActual;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Optional<UsuarioAutenticado> usuario = sesionActual.usuario();
        if (usuario.isEmpty() || !tallerActual.hayTaller()) {
            return true;
        }
        if (usuario.get().taller().equals(tallerActual.id())) {
            return true;
        }

        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"sesion_de_otro_taller\"}");
        return false;
    }
}
