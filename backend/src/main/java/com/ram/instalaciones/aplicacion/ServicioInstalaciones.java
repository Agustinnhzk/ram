package com.ram.instalaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.instalaciones.DatosDeAbonado;
import com.ram.instalaciones.DatosDeInstalacion;
import com.ram.instalaciones.FilaDeAbonos;
import com.ram.instalaciones.Instalacion;
import com.ram.instalaciones.Instalaciones;
import com.ram.instalaciones.NoEncontradoException;
import com.ram.instalaciones.SinPermisoException;
import com.ram.instalaciones.dominio.Abonado;
import com.ram.instalaciones.dominio.FichaDeInstalacion;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.usuarios.UsuarioAutenticado;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Casos de uso de instalaciones y monitoreo (ADR-015). Es un registro: pocas reglas a propósito. */
public class ServicioInstalaciones implements Instalaciones {

    private final InstalacionesRepositorio repositorio;
    private final Clientes clientes;
    private final SesionActual sesion;
    private final Clock reloj;

    public ServicioInstalaciones(InstalacionesRepositorio repositorio, Clientes clientes, SesionActual sesion, Clock reloj) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.clientes = Objects.requireNonNull(clientes);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public List<Instalacion> listar(boolean terminadas) {
        personal();
        List<FichaDeInstalacion> fichas = repositorio.instalaciones(terminadas);
        Map<UUID, DatosCliente> datos = datosDe(fichas.stream().map(FichaDeInstalacion::clienteId).toList());
        return fichas.stream().map(f -> aDatos(f, datos.get(f.clienteId()))).toList();
    }

    @Override
    public Optional<Instalacion> obtener(UUID id) {
        personal();
        return repositorio.instalacion(id).map(f -> aDatos(f, clientes.obtener(ClienteId.de(f.clienteId())).orElse(null)));
    }

    @Override
    public Instalacion crear(DatosDeInstalacion d) {
        personal();
        UUID cliente = clienteDe(d.clienteId(), d.clienteNuevo());
        FichaDeInstalacion ficha = new FichaDeInstalacion(UUID.randomUUID(), repositorio.ultimoNumero() + 1, cliente,
                d.direccion(), d.estado(), d.equipos(), d.camaras(), d.fecha(), d.tecnico(), d.presupuesto(), d.notas(),
                reloj.instant());
        repositorio.guardar(ficha);
        return aDatos(ficha, clientes.obtener(ClienteId.de(cliente)).orElse(null));
    }

    @Override
    public void editar(UUID id, DatosDeInstalacion d) {
        personal();
        FichaDeInstalacion actual = repositorio.instalacion(id).orElseThrow(NoEncontradoException::new);
        repositorio.guardar(new FichaDeInstalacion(actual.id(), actual.numero(), actual.clienteId(), d.direccion(),
                d.estado() == null ? actual.estado() : d.estado(), d.equipos(), d.camaras(), d.fecha(), d.tecnico(),
                d.presupuesto(), d.notas(), actual.creadaEn()));
    }

    // ─── Monitoreo ───────────────────────────────────────────────────────────────────────────

    @Override
    public List<FilaDeAbonos> abonos(int anio) {
        personal();
        validarAnio(anio);
        List<Abonado> abonados = repositorio.abonados();
        Map<UUID, DatosCliente> datos = datosDe(abonados.stream().map(Abonado::clienteId).toList());
        Set<String> pagos = repositorio.pagos(anio);
        return abonados.stream()
                .map(a -> {
                    DatosCliente c = datos.get(a.clienteId());
                    List<Boolean> meses = new ArrayList<>();
                    for (int mes = 1; mes <= 12; mes++) {
                        meses.add(pagos.contains(a.id() + ":" + mes));
                    }
                    return new FilaDeAbonos(a.id(), a.clienteId(), c == null ? "—" : c.nombreCompleto(),
                            c == null ? null : c.telefono(), a.direccion(), a.monto(), a.activo(), meses);
                })
                .sorted(Comparator.comparing(FilaDeAbonos::activo).reversed().thenComparing(FilaDeAbonos::cliente))
                .toList();
    }

    @Override
    public UUID agregarAbonado(DatosDeAbonado d) {
        personal();
        Abonado abonado = new Abonado(UUID.randomUUID(), clienteDe(d.clienteId(), d.clienteNuevo()), d.direccion(), d.monto(), true);
        repositorio.guardar(abonado);
        return abonado.id();
    }

    @Override
    public void editarAbonado(UUID id, DatosDeAbonado d) {
        personal();
        Abonado actual = repositorio.abonado(id).orElseThrow(NoEncontradoException::new);
        repositorio.guardar(new Abonado(actual.id(), actual.clienteId(), d.direccion(), d.monto(), d.activo()));
    }

    @Override
    public void marcarPago(UUID abonado, int anio, int mes, boolean pagado) {
        if (!personal().tieneAlgunRol(Rol.DUENO, Rol.MOSTRADOR)) {
            throw new SinPermisoException("Los pagos los marcan el Dueño y Mostrador");
        }
        validarAnio(anio);
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes tiene que ser de 1 a 12");
        }
        repositorio.abonado(abonado).orElseThrow(NoEncontradoException::new);
        repositorio.marcarPago(abonado, anio, mes, pagado);
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    /** Una ficha que ya existe, o una nueva con nombre y teléfono (como en el ingreso de reparaciones). */
    private UUID clienteDe(UUID existente, DatosDeInstalacion.ClienteNuevo nuevo) {
        if (existente != null) {
            if (clientes.obtener(ClienteId.de(existente)).isEmpty()) {
                throw new IllegalArgumentException("El cliente elegido no existe");
            }
            return existente;
        }
        if (nuevo == null) {
            throw new IllegalArgumentException("Falta el cliente");
        }
        return clientes.registrar(new NuevoCliente(nuevo.nombreCompleto(), nuevo.telefono(), null)).valor();
    }

    private Map<UUID, DatosCliente> datosDe(Collection<UUID> ids) {
        return clientes.obtenerVarios(ids.stream().distinct().map(ClienteId::de).toList()).stream()
                .collect(Collectors.toMap(c -> c.id().valor(), Function.identity()));
    }

    private static Instalacion aDatos(FichaDeInstalacion f, DatosCliente c) {
        return new Instalacion(f.id(), f.numero(), f.clienteId(), c == null ? "—" : c.nombreCompleto(),
                c == null ? null : c.telefono(), f.direccion(), f.estado(), f.equipos(), f.camaras(), f.fecha(), f.tecnico(),
                f.presupuesto(), f.notas(), f.creadaEn());
    }

    private static void validarAnio(int anio) {
        if (anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("Año inválido");
        }
    }

    private UsuarioAutenticado personal() {
        UsuarioAutenticado usuario = sesion.requerido();
        if (!usuario.esPersonal()) {
            throw new SinPermisoException("Solo el personal del taller");
        }
        return usuario;
    }
}
