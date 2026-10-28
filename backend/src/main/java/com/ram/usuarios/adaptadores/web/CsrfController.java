package com.ram.usuarios.adaptadores.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * El frontend llama a esto al cargar, antes de cualquier POST/PUT/DELETE.
 *
 * <p>Pedir el token hace que Spring lo genere y lo deje en la cookie XSRF-TOKEN.
 * Después, cada pedido que modifica algo lo manda de vuelta en el header X-XSRF-TOKEN.
 *
 * <pre>
 * GET /api/publico/csrf  →  {"header":"X-XSRF-TOKEN","token":"..."}
 * </pre>
 */
@RestController
class CsrfController {

    @GetMapping("/api/publico/csrf")
    Map<String, String> token(CsrfToken token) {
        return Map.of("header", token.getHeaderName(), "token", token.getToken());
    }
}
