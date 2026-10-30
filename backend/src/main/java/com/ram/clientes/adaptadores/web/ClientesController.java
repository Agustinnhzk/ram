package com.ram.clientes.adaptadores.web;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.Clientes;
import com.ram.clientes.CuentaYaVinculadaException;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.clientes.SinPermisoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.time.Instant;
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
 * GET    /api/sistema/clientes?nombre=agus        buscar (autocompletado del formulario de ingreso)
 * GET    /api/sistema/clientes?texto=341          pantalla Clientes: por nombre o teléfono; sin texto, los últimos
 * POST   /api/sistema/clientes                    registrar
 * GET    /api/sistema/clientes/{id}               la ficha completa
 * PUT    /api/sistema/clientes/{id}               editar nombre, teléfono, correo y notas
 * PUT    /api/sistema/clientes/{id}/contacto      cambiar teléfono y correo
 * PUT    /api/sistema/clientes/{id}/cuenta        {"correo": "..."}  vincular una cuenta de la tienda
 * DELETE /api/sistema/clientes/{id}/cuenta        desvincularla
 * DELETE /api/sistema/clientes/{id}               borrar la ficha y su cuenta de la tienda (solo Dueño, ADR-016)
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/clientes")
class ClientesController {

    private final Clientes clientes;

    ClientesController(Clientes clientes) {
        this.clientes = clientes;
    }

    /** Dos usos: con "nombre", el autocompletado de reparaciones (como siempre); si no, la pantalla Clientes. */
    @GetMapping
    List<?> buscar(@RequestParam(required = false) String nombre, @RequestParam(required = false) String texto) {
        if (nombre != null) {
            return clientes.buscarPorNombre(nombre).stream().map(ClienteResponse::de).toList();
        }
        return clientes.listar(texto).stream().map(FichaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> registrar(@RequestBody NuevoCliente nuevo) {
        return Map.of("id", clientes.registrar(nuevo).valor());
    }

    @GetMapping("/{id}")
    ResponseEntity<FichaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.of(clientes.ficha(ClienteId.de(id)).map(FichaResponse::de));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable UUID id, @RequestBody DatosDeFicha datos) {
        clientes.editar(ClienteId.de(id), datos);
    }

    @PutMapping("/{id}/contacto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void actualizarContacto(@PathVariable UUID id, @RequestBody ContactoNuevo contacto) {
        clientes.actualizarContacto(ClienteId.de(id), contacto.telefono(), contacto.correo());
    }

    @PutMapping("/{id}/cuenta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void vincularCuenta(@PathVariable UUID id, @RequestBody CorreoJson cuerpo) {
        clientes.vincularCuenta(ClienteId.de(id), cuerpo.correo());
    }

    @DeleteMapping("/{id}/cuenta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desvincularCuenta(@PathVariable UUID id) {
        clientes.desvincularCuenta(ClienteId.de(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrar(@PathVariable UUID id) {
        clientes.borrar(ClienteId.de(id));
    }

    record ContactoNuevo(String telefono, String correo) {
    }

    record CorreoJson(String correo) {
    }

    /** Forma del JSON hacia afuera, independiente de los tipos internos: {"id":"...","nombreCompleto":...}. */
    record ClienteResponse(UUID id, String nombreCompleto, String telefono, String correo) {
        static ClienteResponse de(DatosCliente d) {
            return new ClienteResponse(d.id().valor(), d.nombreCompleto(), d.telefono(), d.correo());
        }
    }

    /** @param cuenta null si no tiene; en el listado viene solo con el id */
    record FichaResponse(UUID id, String nombreCompleto, String telefono, String correo, String notas, CuentaJson cuenta,
                         Instant creadoEn) {
        static FichaResponse de(FichaDeCliente f) {
            return new FichaResponse(f.id().valor(), f.nombreCompleto(), f.telefono(), f.correo(), f.notas(),
                    f.cuenta() == null ? null : new CuentaJson(f.cuenta().id(), f.cuenta().nombre(), f.cuenta().correo()),
                    f.creadoEn());
        }
    }

    record CuentaJson(UUID id, String nombre, String correo) {
    }

    /** Datos inválidos (los rechaza el dominio) → 400 con el motivo, para mostrarlo en el formulario. */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del cliente"));
    }

    @ExceptionHandler(CuentaYaVinculadaException.class)
    ResponseEntity<Map<String, String>> yaVinculada(CuentaYaVinculadaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "cuenta_ya_vinculada", "detalle", e.getMessage()));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }

    /** La base no deja borrar una ficha que otra tabla usa (reparaciones, instalaciones, abonos, pedidos). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> enUso() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "cliente_en_uso", "detalle",
                "No se puede borrar: tiene pedidos, reparaciones, instalaciones o abonos cargados. Borrá primero sus pedidos."));
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado(ClienteNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "cliente_no_encontrado"));
    }
}
