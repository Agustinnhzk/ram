package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.aplicacion.ServicioDeRecuperacion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Locale;
import java.util.Map;

/**
 * "Olvidé mi contraseña" (ADR-012). Sin login; con token CSRF, como el registro.
 *
 * <pre>
 * POST /api/tienda/recuperar-clave     {"correo": "..."}              → 202 SIEMPRE (exista o no la cuenta), 429 si abusa
 * POST /api/tienda/restablecer-clave   {"token": "...", "clave": "..."} → 204; 400 enlace_invalido / datos_invalidos
 * </pre>
 */
@RestController
@RequestMapping("/api/tienda")
class RecuperacionDeClaveController {

    private static final Map<String, String> DEMASIADOS = Map.of(
            "error", "demasiados_intentos", "detalle", "Pediste varios links seguidos. Revisá tu correo o probá en un rato.");

    private final ServicioDeRecuperacion recuperacion;
    private final LimitadorDeRecuperaciones limitador;

    RecuperacionDeClaveController(ServicioDeRecuperacion recuperacion, LimitadorDeRecuperaciones limitador) {
        this.recuperacion = recuperacion;
        this.limitador = limitador;
    }

    @PostMapping("/recuperar-clave")
    ResponseEntity<Map<String, String>> solicitar(@RequestBody Solicitud solicitud, HttpServletRequest request) {
        String correo = solicitud.correo() == null ? "" : solicitud.correo().trim().toLowerCase(Locale.ROOT);
        if (!limitador.puedePedir(request.getRemoteAddr(), correo)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(DEMASIADOS);
        }
        // La dirección de la tienda sale del pedido (con los X-Forwarded-* del proxy): es un dominio de un taller
        // conocido, porque si no lo fuera, el filtro de talleres ya lo habría rechazado.
        String tienda = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        recuperacion.solicitar(correo, tienda);
        return ResponseEntity.accepted().body(Map.of("estado", "si_existe_le_llega_un_mail"));
    }

    @PostMapping("/restablecer-clave")
    ResponseEntity<Void> restablecer(@RequestBody Restablecimiento cuerpo) {
        recuperacion.restablecer(cuerpo.token(), cuerpo.clave());
        return ResponseEntity.noContent().build();
    }

    record Solicitud(String correo) {
    }

    record Restablecimiento(String token, String clave) {
        @Override
        public String toString() {
            return "Restablecimiento[****]";
        }
    }
}
