package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import com.ram.usuarios.adaptadores.seguridad.SesionWeb;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cuenta del cliente en la tienda.
 *
 * <pre>
 * POST   /api/tienda/registro   crear cuenta (y queda logueado)  {"nombre":"...","correo":"...","clave":"..."}
 * POST   /api/tienda/sesion     iniciar sesión                   {"correo":"...","clave":"..."}
 * GET    /api/tienda/sesion     ¿quién soy?
 * DELETE /api/tienda/sesion     cerrar sesión
 * </pre>
 */
@RestController
@RequestMapping("/api/tienda")
class SesionClienteController {

    private final ServicioDeAcceso acceso;
    private final SesionWeb sesionWeb;
    private final LimitadorDeIngresos limitador;

    SesionClienteController(ServicioDeAcceso acceso, SesionWeb sesionWeb, LimitadorDeIngresos limitador) {
        this.acceso = acceso;
        this.sesionWeb = sesionWeb;
        this.limitador = limitador;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    SesionResponse registrar(@RequestBody Registro registro, HttpServletRequest request, HttpServletResponse response) {
        var resultado = acceso.registrarCliente(registro.nombre(), registro.correo(), registro.clave());
        return SesionResponse.de(sesionWeb.iniciar(resultado, request, response));
    }

    @PostMapping("/sesion")
    SesionResponse iniciar(@RequestBody Credenciales credenciales, HttpServletRequest request, HttpServletResponse response) {
        var resultado = limitador.proteger(request.getRemoteAddr(), credenciales.correo(),
                () -> acceso.iniciarSesionCliente(credenciales.correo(), credenciales.clave()));
        return SesionResponse.de(sesionWeb.iniciar(resultado, request, response));
    }

    /**
     * Quién está logueado. Sin sesión de cliente responde 204 (vacío), no 401: la tienda lo pregunta en cada
     * página para armar el encabezado, y no estar logueado ahí es lo normal, no un error.
     */
    @GetMapping("/sesion")
    ResponseEntity<SesionResponse> actual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        if (usuario == null || usuario.tipo() != TipoDeCuenta.CLIENTE) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(SesionResponse.de(usuario));
    }

    @DeleteMapping("/sesion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cerrar(HttpServletRequest request, HttpServletResponse response) {
        sesionWeb.cerrar(request, response);
    }

    record Registro(String nombre, String correo, String clave) {
        @Override
        public String toString() {
            return "Registro[nombre=" + nombre + ", correo=" + correo + ", clave=****]";
        }
    }
}
