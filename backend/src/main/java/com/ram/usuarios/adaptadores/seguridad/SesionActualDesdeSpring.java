package com.ram.usuarios.adaptadores.seguridad;

import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SesionActualDesdeSpring implements SesionActual {

    @Override
    public Optional<UsuarioAutenticado> usuario() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }
}
