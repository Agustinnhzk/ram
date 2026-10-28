package com.ram.tenants.adaptadores.web;

import com.ram.tenants.dominio.Taller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Datos públicos del taller de esta dirección. La tienda lo va a usar para mostrar
 * el nombre del taller; más adelante se suman logo, redes y QR fiscal.
 *
 * <p>GET /api/publico/taller → {"nombre":"RAM","slug":"ram"}
 */
@RestController
@RequestMapping("/api/publico/taller")
class TallerPublicoController {

    @GetMapping
    TallerPublico actual(@RequestAttribute(ResolucionTallerFilter.ATRIBUTO_TALLER) Taller taller) {
        return new TallerPublico(taller.nombre(), taller.slug().valor());
    }

    /** Lo que se expone hacia afuera: nunca el objeto de dominio entero (ni su id interno). */
    record TallerPublico(String nombre, String slug) {
    }
}
