package com.ram.pedidos.adaptadores.web;

import com.ram.pedidos.Pedidos;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <pre>
 * GET /api/publico/armado-de-pc          → {"precio": 30000}. Lo muestra Armá tu PC, sin login. 404 si no se ofrece.
 * PUT /api/sistema/ajustes/armado-de-pc  {"precio": 35000} o {"precio": null} (deja de ofrecerlo). Solo el Dueño.
 * </pre>
 */
@RestController
class ArmadoDePcController {

    private final Pedidos pedidos;

    ArmadoDePcController(Pedidos pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping("/api/publico/armado-de-pc")
    ResponseEntity<Map<String, Long>> precio() {
        return ResponseEntity.of(pedidos.precioDelArmado().map(precio -> Map.of("precio", precio)));
    }

    @PutMapping("/api/sistema/ajustes/armado-de-pc")
    ResponseEntity<Void> cambiar(@RequestBody PrecioJson cuerpo) {
        pedidos.cambiarPrecioDelArmado(cuerpo.precio());
        return ResponseEntity.noContent().build();
    }

    record PrecioJson(Long precio) {
    }
}
