package com.ram.reparaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.reparaciones.DatosMovimiento;
import com.ram.reparaciones.DetalleDeOrden;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.NuevoIngreso;
import com.ram.reparaciones.NumeroDeOrdenRepetidoException;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.OrdenNoEncontradaException;
import com.ram.reparaciones.Reparaciones;
import com.ram.reparaciones.ResumenDeOrden;
import com.ram.reparaciones.Seguimiento;
import com.ram.reparaciones.VistaDeOrdenes;
import com.ram.reparaciones.dominio.Autor;
import com.ram.reparaciones.dominio.Equipo;
import com.ram.reparaciones.dominio.OrdenDeReparacion;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Casos de uso de reparaciones. Implementa la API pública del módulo.
 *
 * <p>Todo el personal puede cargar y mover órdenes. Lo único restringido por rol es el password
 * del equipo: lo ven el Dueño y el Técnico, no Mostrador (ADR-009).
 */
public class ServicioReparaciones implements Reparaciones {

    static final int LIMITE_LISTADO = 200;

    private final OrdenesRepositorio ordenes;
    private final Clientes clientes;
    private final SesionActual sesion;
    private final Clock reloj;
    private final ZoneId zonaDelTaller;

    public ServicioReparaciones(OrdenesRepositorio ordenes, Clientes clientes, SesionActual sesion, Clock reloj, ZoneId zonaDelTaller) {
        this.ordenes = Objects.requireNonNull(ordenes);
        this.clientes = Objects.requireNonNull(clientes);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
        this.zonaDelTaller = Objects.requireNonNull(zonaDelTaller);
    }

    @Override
    public OptionalInt ultimoNumero() {
        return ordenes.ultimoNumero();
    }

    @Override
    public OrdenId registrarIngreso(NuevoIngreso ingreso) {
        if (ingreso.numero() == null) {
            throw new IllegalArgumentException("Falta el número de orden");
        }
        if (ordenes.existeNumero(ingreso.numero())) {
            throw new NumeroDeOrdenRepetidoException(ingreso.numero());
        }
        // Todo el ingreso es UNA transacción (ver ReparacionesTransaccional): si algún dato es inválido,
        // tampoco queda guardado el cliente nuevo. Igual se valida el equipo primero, para fallar antes.
        Equipo equipo = new Equipo(ingreso.equipo(), ingreso.conCargador(), ingreso.procesador());
        ClienteId cliente = clienteDe(ingreso);

        OrdenDeReparacion orden = OrdenDeReparacion.ingresar(ingreso.numero(), cliente, equipo, ingreso.password(),
                ingreso.problema(), ingreso.observaciones(), autor(), reloj.instant());
        ordenes.guardar(orden);
        return orden.id();
    }

    @Override
    public List<ResumenDeOrden> listar(VistaDeOrdenes vista, String texto) {
        List<OrdenDeReparacion> encontradas = buscar(vista, texto);
        Map<ClienteId, DatosCliente> datosDeClientes = clientes.obtenerVarios(
                        encontradas.stream().map(OrdenDeReparacion::cliente).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(DatosCliente::id, Function.identity()));
        LocalDate hoy = hoy();

        return encontradas.stream().map(o -> {
            DatosCliente c = datosDeClientes.get(o.cliente());
            return new ResumenDeOrden(o.id(), o.numero(), c == null ? "—" : c.nombreCompleto(), c == null ? null : c.telefono(),
                    o.equipo().descripcion(), o.estado(), o.ingresadoEn(), o.garantiaHasta(), o.enGarantia(hoy));
        }).toList();
    }

    @Override
    public List<ResumenDeOrden> deCliente(ClienteId cliente) {
        Optional<DatosCliente> datos = clientes.obtener(cliente);
        if (datos.isEmpty()) {
            return List.of();
        }
        LocalDate hoy = hoy();
        DatosCliente c = datos.get();
        return ordenes.deClientes(List.of(cliente), LIMITE_LISTADO).stream()
                .map(o -> new ResumenDeOrden(o.id(), o.numero(), c.nombreCompleto(), c.telefono(), o.equipo().descripcion(),
                        o.estado(), o.ingresadoEn(), o.garantiaHasta(), o.enGarantia(hoy)))
                .toList();
    }

    @Override
    public List<Seguimiento> misReparaciones() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (usuario.esPersonal()) {
            return List.of();
        }
        return clientes.deLaCuenta(usuario.id())
                .map(cliente -> ordenes.deClientes(List.of(cliente), LIMITE_LISTADO).stream().map(this::seguimientoDe).toList())
                .orElse(List.of());
    }

    @Override
    public Optional<DetalleDeOrden> obtener(OrdenId id) {
        boolean puedeVerPassword = sesion.requerido().tieneAlgunRol(Rol.DUENO, Rol.TECNICO);
        return ordenes.porId(id).map(o -> new DetalleDeOrden(
                o.id(),
                o.numero(),
                clientes.obtener(o.cliente()).orElse(null),
                o.equipo().descripcion(),
                o.equipo().conCargador(),
                o.equipo().procesador(),
                puedeVerPassword ? o.password() : null,
                puedeVerPassword,
                o.problema(),
                o.observaciones(),
                o.estado(),
                o.estado().siguientes(),
                o.presupuestoMonto(),
                o.presupuestoDescripcion(),
                o.ingresadoEn(),
                o.entregadoEn(),
                o.garantiaDias(),
                o.garantiaHasta(),
                o.enGarantia(hoy()),
                o.movimientos().stream()
                        .map(m -> new DatosMovimiento(m.estado(), m.nota(), m.paraCliente(), m.autor().nombre(), m.cuando()))
                        .toList()));
    }

    @Override
    public void cambiarEstado(OrdenId id, EstadoReparacion destino, String nota, boolean notaParaCliente) {
        OrdenDeReparacion orden = cargar(id);
        orden.cambiarEstado(destino, nota, notaParaCliente, autor(), reloj.instant());
        ordenes.guardar(orden);
    }

    @Override
    public void presupuestar(OrdenId id, long monto, String descripcion) {
        OrdenDeReparacion orden = cargar(id);
        orden.presupuestar(monto, descripcion, autor(), reloj.instant());
        ordenes.guardar(orden);
    }

    @Override
    public void entregar(OrdenId id, int garantiaDias, String nota) {
        OrdenDeReparacion orden = cargar(id);
        orden.entregar(garantiaDias, nota, autor(), reloj.instant(), zonaDelTaller);
        ordenes.guardar(orden);
    }

    @Override
    public void agregarNota(OrdenId id, String texto, boolean paraCliente) {
        OrdenDeReparacion orden = cargar(id);
        orden.agregarNota(texto, paraCliente, autor(), reloj.instant());
        ordenes.guardar(orden);
    }

    @Override
    public Optional<Seguimiento> seguimiento(int numero, String ultimosDigitosDelTelefono) {
        if (ultimosDigitosDelTelefono == null || !ultimosDigitosDelTelefono.matches("\\d{4}")) {
            return Optional.empty();
        }
        return ordenes.porNumero(numero)
                .filter(o -> clientes.obtener(o.cliente())
                        .map(c -> c.telefono().endsWith(ultimosDigitosDelTelefono))
                        .orElse(false))
                .map(this::seguimientoDe);
    }

    /** Lo que ve el cliente: solo lo marcado para él. */
    private Seguimiento seguimientoDe(OrdenDeReparacion o) {
        return new Seguimiento(
                o.numero(),
                o.equipo().descripcion(),
                o.estado(),
                o.presupuestoMonto(),
                o.presupuestoDescripcion(),
                o.ingresadoEn(),
                o.garantiaHasta(),
                o.enGarantia(hoy()),
                o.movimientos().stream()
                        .filter(m -> m.paraCliente())
                        .map(m -> new Seguimiento.Paso(m.estado(), m.nota(), m.cuando()))
                        .toList());
    }

    // ─── Ayudas ──────────────────────────────────────────────────────────────────────────────

    private List<OrdenDeReparacion> buscar(VistaDeOrdenes vista, String texto) {
        Set<EstadoReparacion> estados = switch (vista == null ? VistaDeOrdenes.EN_TALLER : vista) {
            case EN_TALLER -> Arrays.stream(EstadoReparacion.values()).filter(EstadoReparacion::enElTaller)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(EstadoReparacion.class)));
            case ENTREGADAS -> EnumSet.of(EstadoReparacion.ENTREGADO);
            case TODAS -> EnumSet.allOf(EstadoReparacion.class);
        };
        String buscado = texto == null ? "" : texto.strip();
        if (buscado.isEmpty()) {
            return ordenes.conEstados(estados, LIMITE_LISTADO);
        }
        // Un número: es esa orden, esté donde esté (aunque ya se haya entregado).
        if (buscado.matches("\\d{1,7}")) {
            return ordenes.porNumero(Integer.parseInt(buscado)).map(List::of).orElse(List.of());
        }
        // Si no, parte del nombre del cliente ("agustin", "perez").
        List<ClienteId> encontrados = clientes.buscarPorNombre(buscado).stream().map(DatosCliente::id).toList();
        if (encontrados.isEmpty()) {
            return List.of();
        }
        return ordenes.deClientes(encontrados, LIMITE_LISTADO).stream().filter(o -> estados.contains(o.estado())).toList();
    }

    private ClienteId clienteDe(NuevoIngreso ingreso) {
        if (ingreso.clienteId() != null) {
            ClienteId id = ClienteId.de(ingreso.clienteId());
            if (clientes.obtener(id).isEmpty()) {
                throw new IllegalArgumentException("El cliente elegido no existe");
            }
            return id;
        }
        if (ingreso.clienteNuevo() == null) {
            throw new IllegalArgumentException("Falta el cliente");
        }
        return clientes.registrar(ingreso.clienteNuevo());
    }

    private OrdenDeReparacion cargar(OrdenId id) {
        return ordenes.porId(id).orElseThrow(() -> new OrdenNoEncontradaException(id));
    }

    private Autor autor() {
        UsuarioAutenticado usuario = sesion.requerido();
        return new Autor(usuario.usuarioId(), usuario.nombre());
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj.withZone(zonaDelTaller));
    }
}
