package com.ram.clientes.adaptadores.web;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints del sistema de gestión para clientes.
 *
 * <p>Como todo /api/sistema, solo responde a personal del taller logueado (ADR-006):
 * la regla está en la configuración de seguridad, no acá.
 *
 * <pre>
 * GET  /api/sistema/clientes?nombre=agus        buscar (autocompletado del formulario de ingreso)
 * POST /api/sistema/clientes                    registrar
 * GET  /api/sistema/clientes/{id}               ver uno
 * PUT  /api/sistema/clientes/{id}/contacto      cambiar teléfono y correo
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/clientes")
class ClientesController {

    private final Clientes clientes;

    ClientesController(Clientes clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    List<ClienteResponse> buscar(@RequestParam String nombre) {
        return clientes.buscarPorNombre(nombre).stream().map(ClienteResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> registrar(@RequestBody NuevoCliente nuevo) {
        return Map.of("id", clientes.registrar(nuevo).valor());
    }

    @GetMapping("/{id}")
    ResponseEntity<ClienteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.of(clientes.obtener(ClienteId.de(id)).map(ClienteResponse::de));
    }

    @PutMapping("/{id}/contacto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void actualizarContacto(@PathVariable UUID id, @RequestBody ContactoNuevo contacto) {
        clientes.actualizarContacto(ClienteId.de(id), contacto.telefono(), contacto.correo());
    }

    record ContactoNuevo(String telefono, String correo) {
    }

    /** Forma del JSON hacia afuera, independiente de los tipos internos: {"id":"...","nombreCompleto":...}. */
    record ClienteResponse(UUID id, String nombreCompleto, String telefono, String correo) {
        static ClienteResponse de(DatosCliente d) {
            return new ClienteResponse(d.id().valor(), d.nombreCompleto(), d.telefono(), d.correo());
        }
    }

    /** Datos inválidos (los rechaza el dominio) → 400 con el motivo, para mostrarlo en el formulario. */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado(ClienteNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "cliente_no_encontrado"));
    }
}
