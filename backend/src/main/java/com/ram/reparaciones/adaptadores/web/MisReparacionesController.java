package com.ram.reparaciones.adaptadores.web;

import com.ram.reparaciones.Reparaciones;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Las reparaciones del cliente logueado en la tienda (ADR-014). Solo aparecen si su cuenta está vinculada
 * a su ficha del taller (pasa sola con su primer pedido, o el taller la vincula a mano).
 *
 * <pre>
 * GET /api/tienda/reparaciones     lo mismo que el seguimiento, para cada una
 * </pre>
 */
@RestController
class MisReparacionesController {

    private final Reparaciones reparaciones;

    MisReparacionesController(Reparaciones reparaciones) {
        this.reparaciones = reparaciones;
    }

    @GetMapping("/api/tienda/reparaciones")
    List<SeguimientoController.SeguimientoJson> mias() {
        return reparaciones.misReparaciones().stream().map(SeguimientoController.SeguimientoJson::de).toList();
    }
}
