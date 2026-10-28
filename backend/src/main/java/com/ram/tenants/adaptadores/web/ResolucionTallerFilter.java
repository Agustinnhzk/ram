package com.ram.tenants.adaptadores.web;

import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import com.ram.tenants.aplicacion.ResolverTaller;
import com.ram.tenants.aplicacion.TallerNoEncontradoException;
import com.ram.tenants.aplicacion.TallerSuspendidoException;
import com.ram.tenants.dominio.Taller;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adaptador de entrada: antes de que cualquier request a /api llegue a un controlador,
 * averigua de qué taller es y lo deja disponible para el resto del sistema.
 *
 * <pre>
 * request → ¿qué host? → ResolverTaller → ContextoTaller → controlador → limpiar contexto
 *                              └── no existe → 404     └── suspendido → 403
 * </pre>
 */
@Component
class ResolucionTallerFilter extends OncePerRequestFilter {

    /** Nombre del atributo de la request donde queda el taller completo, para los controladores. */
    static final String ATRIBUTO_TALLER = "ram.taller";

    private final ResolverTaller resolverTaller;

    ResolucionTallerFilter(ResolverTaller resolverTaller) {
        this.resolverTaller = resolverTaller;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo la API depende del taller. Health checks u otras rutas técnicas quedan afuera.
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Taller taller;
        try {
            // getServerName() es el host sin puerto: "ram.localhost", "ramservicios.com.ar".
            taller = resolverTaller.ejecutar(request.getServerName());
        } catch (TallerNoEncontradoException e) {
            responderError(response, HttpServletResponse.SC_NOT_FOUND, "taller_no_encontrado");
            return;
        } catch (TallerSuspendidoException e) {
            responderError(response, HttpServletResponse.SC_FORBIDDEN, "taller_suspendido");
            return;
        }

        ContextoTaller.establecer(taller.id());
        request.setAttribute(ATRIBUTO_TALLER, taller);
        try {
            chain.doFilter(request, response);
        } finally {
            ContextoTaller.limpiar();
        }
    }

    private static void responderError(HttpServletResponse response, int status, String codigo) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + codigo + "\"}");
    }
}
