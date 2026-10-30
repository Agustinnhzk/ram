package com.ram.reparaciones.adaptadores.web;

import com.ram.clientes.ClienteId;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.reparaciones.DatosMovimiento;
import com.ram.reparaciones.DetalleDeOrden;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.NuevoIngreso;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.ResumenDeOrden;
import com.ram.reparaciones.VistaDeOrdenes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reparaciones en el sistema de gestión. Solo personal logueado (SeguridadConfig).
 *
 * <pre>
 * GET  /api/sistema/reparaciones?vista=EN_TALLER&q=perez   listado (vista: EN_TALLER, ENTREGADAS, TODAS)
 * GET  /api/sistema/reparaciones/ultimo-numero             {"ultimo": 901} (o null)
 * POST /api/sistema/reparaciones                           ingreso → {"id": "..."}
 * GET  /api/sistema/reparaciones/{id}                      detalle con bitácora
 * POST /api/sistema/reparaciones/{id}/estado               {"estado": "EN_DIAGNOSTICO", "nota": "...", "notaParaCliente": false}
 * POST /api/sistema/reparaciones/{id}/presupuesto          {"monto": 110000, "descripcion": "Cambio de pantalla"}
 * POST /api/sistema/reparaciones/{id}/entrega              {"garantiaDias": 60, "nota": "..."}
 * POST /api/sistema/reparaciones/{id}/notas                {"texto": "...", "paraCliente": true}
 * </pre>
 */
@RestController
@RequestMapping("/api/sistema/reparaciones")
class ReparacionesController {

    private final Reparaciones reparaciones;

    ReparacionesController(Reparaciones reparaciones) {
        this.reparaciones = reparaciones;
    }

    @GetMapping
    List<ResumenJson> listar(@RequestParam(defaultValue = "EN_TALLER") VistaDeOrdenes vista,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) UUID cliente) {
        if (cliente != null) {
            return reparaciones.deCliente(ClienteId.de(cliente)).stream().map(ResumenJson::de).toList();
        }
        return reparaciones.listar(vista, q).stream().map(ResumenJson::de).toList();
    }

    @GetMapping("/ultimo-numero")
    Map<String, Integer> ultimoNumero() {
        var ultimo = reparaciones.ultimoNumero();
        return java.util.Collections.singletonMap("ultimo", ultimo.isPresent() ? ultimo.getAsInt() : null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> ingresar(@RequestBody Ingreso ingreso) {
        return Map.of("id", reparaciones.registrarIngreso(ingreso.aNuevoIngreso()).valor());
    }

    @GetMapping("/{id}")
    ResponseEntity<DetalleJson> obtener(@PathVariable UUID id) {
        return ResponseEntity.of(reparaciones.obtener(OrdenId.de(id)).map(DetalleJson::de));
    }

    @PostMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiarEstado(@PathVariable UUID id, @RequestBody CambioDeEstado cambio) {
        reparaciones.cambiarEstado(OrdenId.de(id), cambio.estado(), cambio.nota(), Boolean.TRUE.equals(cambio.notaParaCliente()));
    }

    @PostMapping("/{id}/presupuesto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void presupuestar(@PathVariable UUID id, @RequestBody Presupuesto presupuesto) {
        if (presupuesto.monto() == null) {
            throw new IllegalArgumentException("Falta el monto del presupuesto");
        }
        reparaciones.presupuestar(OrdenId.de(id), presupuesto.monto(), presupuesto.descripcion());
    }

    @PostMapping("/{id}/entrega")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void entregar(@PathVariable UUID id, @RequestBody Entrega entrega) {
        if (entrega.garantiaDias() == null) {
            throw new IllegalArgumentException("Falta la garantía (0 si no tiene)");
        }
        reparaciones.entregar(OrdenId.de(id), entrega.garantiaDias(), entrega.nota());
    }

    @PostMapping("/{id}/notas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void agregarNota(@PathVariable UUID id, @RequestBody Nota nota) {
        reparaciones.agregarNota(OrdenId.de(id), nota.texto(), Boolean.TRUE.equals(nota.paraCliente()));
    }

    // ─── Lo que llega ─────────────────────────────────────────────────────────────────────────

    record ClienteNuevo(String nombreCompleto, String telefono, String correo) {
    }

    /** Boolean (no boolean) en los opcionales: si no vienen en el JSON quedan null, no es un error. */
    record Ingreso(Integer numero, UUID clienteId, ClienteNuevo clienteNuevo, String equipo, Boolean conCargador,
                   String procesador, String password, String problema, String observaciones) {

        NuevoIngreso aNuevoIngreso() {
            NuevoCliente nuevo = clienteNuevo == null ? null
                    : new NuevoCliente(clienteNuevo.nombreCompleto(), clienteNuevo.telefono(), clienteNuevo.correo());
            return new NuevoIngreso(numero, clienteId, nuevo, equipo, Boolean.TRUE.equals(conCargador), procesador,
                    password, problema, observaciones);
        }

        @Override
        public String toString() {
            return "Ingreso[numero=" + numero + "]";
        }
    }

    record CambioDeEstado(EstadoReparacion estado, String nota, Boolean notaParaCliente) {
    }

    record Presupuesto(Long monto, String descripcion) {
    }

    record Entrega(Integer garantiaDias, String nota) {
    }

    record Nota(String texto, Boolean paraCliente) {
    }

    // ─── Lo que sale ──────────────────────────────────────────────────────────────────────────

    record ResumenJson(UUID id, int numero, String cliente, String telefono, String equipo, EstadoReparacion estado,
                       Instant ingresadoEn, LocalDate garantiaHasta, boolean enGarantia) {
        static ResumenJson de(ResumenDeOrden r) {
            return new ResumenJson(r.id().valor(), r.numero(), r.cliente(), r.telefono(), r.equipo(), r.estado(),
                    r.ingresadoEn(), r.garantiaHasta(), r.enGarantia());
        }
    }

    record ClienteJson(UUID id, String nombreCompleto, String telefono, String correo) {
        static ClienteJson de(DatosCliente c) {
            return c == null ? null : new ClienteJson(c.id().valor(), c.nombreCompleto(), c.telefono(), c.correo());
        }
    }

    record MovimientoJson(EstadoReparacion estado, String nota, boolean paraCliente, String usuario, Instant cuando) {
        static MovimientoJson de(DatosMovimiento m) {
            return new MovimientoJson(m.estado(), m.nota(), m.paraCliente(), m.usuario(), m.cuando());
        }
    }

    record DetalleJson(UUID id, int numero, ClienteJson cliente, String equipo, boolean conCargador, String procesador,
                       String password, boolean passwordVisible, String problema, String observaciones,
                       EstadoReparacion estado, Set<EstadoReparacion> siguientes, Long presupuestoMonto,
                       String presupuestoDescripcion, Instant ingresadoEn, Instant entregadoEn, Integer garantiaDias,
                       LocalDate garantiaHasta, boolean enGarantia, List<MovimientoJson> movimientos) {
        static DetalleJson de(DetalleDeOrden d) {
            return new DetalleJson(d.id().valor(), d.numero(), ClienteJson.de(d.cliente()), d.equipo(), d.conCargador(),
                    d.procesador(), d.password(), d.passwordVisible(), d.problema(), d.observaciones(), d.estado(),
                    d.siguientes(), d.presupuestoMonto(), d.presupuestoDescripcion(), d.ingresadoEn(), d.entregadoEn(),
                    d.garantiaDias(), d.garantiaHasta(), d.enGarantia(),
                    d.movimientos().stream().map(MovimientoJson::de).toList());
        }

        @Override
        public String toString() {
            return "DetalleJson[numero=" + numero + "]";
        }
    }
}
