package com.ram.reparaciones.adaptadores.web;

import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.Seguimiento;
import com.ram.tenants.TallerActual;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * El seguimiento que ve el cliente, sin login (ADR-009).
 *
 * <pre>
 * POST /api/publico/seguimiento   {"numero": 901, "telefono": "7613"}
 *   200  el seguimiento
 *   404  "no encontramos una reparación con esos datos" (igual si el número no existe o los dígitos no coinciden)
 *   429  demasiados intentos
 * </pre>
 *
 * <p>Es POST y no GET para que los dígitos del teléfono no queden en la URL (ni en historiales ni en logs).
 */
@RestController
class SeguimientoController {

    private static final Map<String, String> NO_ENCONTRADA = Map.of(
            "error", "no_encontrada", "detalle", "No encontramos una reparación con esos datos.");
    private static final Map<String, String> DEMASIADOS_INTENTOS = Map.of(
            "error", "demasiados_intentos", "detalle", "Hiciste muchos intentos. Probá de nuevo en 15 minutos.");

    private final Reparaciones reparaciones;
    private final LimitadorDeIntentos limitador;
    private final TallerActual tallerActual;

    SeguimientoController(Reparaciones reparaciones, LimitadorDeIntentos limitador, TallerActual tallerActual) {
        this.reparaciones = reparaciones;
        this.limitador = limitador;
        this.tallerActual = tallerActual;
    }

    @PostMapping("/api/publico/seguimiento")
    ResponseEntity<?> consultar(@RequestBody Consulta consulta, HttpServletRequest request) {
        if (consulta.numero() == null || consulta.numero() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(NO_ENCONTRADA);
        }
        // La clave incluye el taller: el número 901 de un taller no bloquea al 901 de otro.
        String orden = tallerActual.id() + ":" + consulta.numero();
        if (!limitador.puedeIntentar(request.getRemoteAddr(), orden)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(DEMASIADOS_INTENTOS);
        }

        Optional<Seguimiento> seguimiento = reparaciones.seguimiento(consulta.numero(), ultimosCuatro(consulta.telefono()));
        if (seguimiento.isEmpty()) {
            limitador.registrarFallo(orden);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(NO_ENCONTRADA);
        }
        limitador.registrarExito(orden);
        return ResponseEntity.ok(SeguimientoJson.de(seguimiento.get()));
    }

    /** Si el cliente escribió el teléfono entero ("341 784-7613"), se toman los últimos 4 dígitos. */
    private static String ultimosCuatro(String telefono) {
        if (telefono == null) {
            return null;
        }
        String digitos = telefono.replaceAll("\\D", "");
        return digitos.length() < 4 ? digitos : digitos.substring(digitos.length() - 4);
    }

    record Consulta(Integer numero, String telefono) {
        /** Sin los dígitos del teléfono, por si algún log imprime el pedido. */
        @Override
        public String toString() {
            return "Consulta[numero=" + numero + "]";
        }
    }

    record PasoJson(EstadoReparacion estado, String nota, Instant cuando) {
    }

    record SeguimientoJson(int numero, String equipo, EstadoReparacion estado, Long presupuestoMonto,
                           String presupuestoDescripcion, Instant ingresadoEn, LocalDate garantiaHasta, boolean enGarantia,
                           List<PasoJson> pasos) {
        static SeguimientoJson de(Seguimiento s) {
            return new SeguimientoJson(s.numero(), s.equipo(), s.estado(), s.presupuestoMonto(), s.presupuestoDescripcion(),
                    s.ingresadoEn(), s.garantiaHasta(), s.enGarantia(),
                    s.pasos().stream().map(p -> new PasoJson(p.estado(), p.nota(), p.cuando())).toList());
        }
    }
}
