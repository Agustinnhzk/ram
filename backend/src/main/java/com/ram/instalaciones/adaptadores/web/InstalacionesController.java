package com.ram.instalaciones.adaptadores.web;

import com.ram.instalaciones.DatosDeAbonado;
import com.ram.instalaciones.DatosDeInstalacion;
import com.ram.instalaciones.FilaDeAbonos;
import com.ram.instalaciones.Instalacion;
import com.ram.instalaciones.Instalaciones;
import com.ram.instalaciones.NoEncontradoException;
import com.ram.instalaciones.SinPermisoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Instalaciones y monitoreo (ADR-015). Solo personal (/api/sistema).
 *
 * <pre>
 * GET  /api/sistema/instalaciones?terminadas=false
 * POST /api/sistema/instalaciones                      {clienteId | clienteNuevo, direccion, estado, equipos, ...}
 * GET  /api/sistema/instalaciones/{id}
 * PUT  /api/sistema/instalaciones/{id}
 * GET  /api/sistema/monitoreo?anio=2026                 abonados con sus 12 meses
 * POST /api/sistema/monitoreo                           {clienteId | clienteNuevo, direccion, monto}
 * PUT  /api/sistema/monitoreo/{id}                      {direccion, monto, activo}
 * PUT  /api/sistema/monitoreo/{id}/pagos/{anio}/{mes}   {"pagado": true}
 * </pre>
 */
@RestController
class InstalacionesController {

    private final Instalaciones instalaciones;

    InstalacionesController(Instalaciones instalaciones) {
        this.instalaciones = instalaciones;
    }

    @GetMapping("/api/sistema/instalaciones")
    List<Instalacion> listar(@RequestParam(defaultValue = "false") boolean terminadas) {
        return instalaciones.listar(terminadas);
    }

    @PostMapping("/api/sistema/instalaciones")
    @ResponseStatus(HttpStatus.CREATED)
    Instalacion crear(@RequestBody DatosDeInstalacion datos) {
        return instalaciones.crear(datos);
    }

    @GetMapping("/api/sistema/instalaciones/{id}")
    ResponseEntity<Instalacion> obtener(@PathVariable UUID id) {
        return ResponseEntity.of(instalaciones.obtener(id));
    }

    @PutMapping("/api/sistema/instalaciones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable UUID id, @RequestBody DatosDeInstalacion datos) {
        instalaciones.editar(id, datos);
    }

    @GetMapping("/api/sistema/monitoreo")
    List<FilaDeAbonos> abonos(@RequestParam int anio) {
        return instalaciones.abonos(anio);
    }

    @PostMapping("/api/sistema/monitoreo")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> agregar(@RequestBody DatosDeAbonado datos) {
        return Map.of("id", instalaciones.agregarAbonado(datos));
    }

    @PutMapping("/api/sistema/monitoreo/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editarAbonado(@PathVariable UUID id, @RequestBody DatosDeAbonado datos) {
        instalaciones.editarAbonado(id, datos);
    }

    @PutMapping("/api/sistema/monitoreo/{id}/pagos/{anio}/{mes}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarPago(@PathVariable UUID id, @PathVariable int anio, @PathVariable int mes, @RequestBody Map<String, Boolean> cuerpo) {
        instalaciones.marcarPago(id, anio, mes, Boolean.TRUE.equals(cuerpo.get("pagado")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos"));
    }

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "no_encontrado"));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }
}
