package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.UsuarioAutenticado;
import com.ram.usuarios.adaptadores.seguridad.SesionWeb;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login del personal en el sistema de gestión.
 *
 * <pre>
 * POST   /api/sistema/sesion    iniciar sesión   {"correo":"...","clave":"..."}
 * GET    /api/sistema/sesion    ¿quién soy?      (401 si no hay sesión)
 * DELETE /api/sistema/sesion    cerrar sesión
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/sesion")
class SesionPersonalController {

    private final ServicioDeAcceso acceso;
    private final SesionWeb sesionWeb;

    SesionPersonalController(ServicioDeAcceso acceso, SesionWeb sesionWeb) {
        this.acceso = acceso;
        this.sesionWeb = sesionWeb;
    }

    @PostMapping
    SesionResponse iniciar(@RequestBody Credenciales credenciales, HttpServletRequest request, HttpServletResponse response) {
        var resultado = acceso.iniciarSesionPersonal(credenciales.correo(), credenciales.clave());
        return SesionResponse.de(sesionWeb.iniciar(resultado, request, response));
    }

    @GetMapping
    SesionResponse actual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return SesionResponse.de(usuario);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cerrar(HttpServletRequest request, HttpServletResponse response) {
        sesionWeb.cerrar(request, response);
    }
}
