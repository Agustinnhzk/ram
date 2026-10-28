package com.ram.usuarios.adaptadores.seguridad;

import com.ram.tenants.TallerActual;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import com.ram.usuarios.aplicacion.Acceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convierte un login exitoso en una sesión HTTP, y la cierra.
 *
 * <p>Es la frontera entre el caso de uso (que solo dice "sí, puede entrar") y la web.
 */
@Component
public class SesionWeb {

    private final SecurityContextRepository repositorioDeContexto;
    private final TallerActual tallerActual;
    private final SecurityContextLogoutHandler cierre = new SecurityContextLogoutHandler();

    SesionWeb(SecurityContextRepository repositorioDeContexto, TallerActual tallerActual) {
        this.repositorioDeContexto = repositorioDeContexto;
        this.tallerActual = tallerActual;
    }

    public UsuarioAutenticado iniciar(Acceso acceso, HttpServletRequest request, HttpServletResponse response) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                acceso.id().valor(), tallerActual.id().valor(), acceso.nombre(), acceso.tipo(), acceso.rol());

        Authentication autenticacion = UsernamePasswordAuthenticationToken.authenticated(usuario, null, permisos(usuario));

        // Fijación de sesión: si el navegador ya traía una sesión (por ejemplo, de antes de loguearse),
        // se le cambia el id. Así, un id de sesión que alguien haya conseguido antes del login no sirve después.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        repositorioDeContexto.saveContext(contexto, request, response);
        return usuario;
    }

    /** Borra la sesión del servidor: aunque alguien tenga la cookie, ya no sirve. */
    public void cerrar(HttpServletRequest request, HttpServletResponse response) {
        cierre.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Personal: ROLE_PERSONAL (para entrar al sistema) + su rol (ROLE_DUENO, ROLE_TECNICO, ROLE_MOSTRADOR).
     * Cliente: ROLE_CLIENTE. Por eso un cliente nunca pasa la regla de /api/sistema.
     */
    private static List<GrantedAuthority> permisos(UsuarioAutenticado usuario) {
        if (usuario.tipo() == TipoDeCuenta.CLIENTE) {
            return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }
        return List.of(
                new SimpleGrantedAuthority("ROLE_PERSONAL"),
                new SimpleGrantedAuthority("ROLE_" + usuario.rol().name()));
    }
}
