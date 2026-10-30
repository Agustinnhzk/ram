package com.ram.usuarios.adaptadores.seguridad;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.aplicacion.SesionesDeUsuario;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

/**
 * Spring Session guarda, para cada sesión, el "nombre" de quien está logueado: el id del usuario
 * (UsuarioAutenticado.getName()). Con eso se encuentran todas sus sesiones y se borran.
 */
@Component
class SesionesSpring implements SesionesDeUsuario {

    private final FindByIndexNameSessionRepository<? extends Session> sesiones;

    SesionesSpring(FindByIndexNameSessionRepository<? extends Session> sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public void cerrarTodas(UsuarioId usuario) {
        sesiones.findByPrincipalName(usuario.valor().toString()).keySet().forEach(sesiones::deleteById);
    }
}
