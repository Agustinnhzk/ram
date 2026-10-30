package com.ram.instalaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.Clientes;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.instalaciones.DatosDeAbonado;
import com.ram.instalaciones.DatosDeInstalacion;
import com.ram.instalaciones.EstadoInstalacion;
import com.ram.instalaciones.Instalacion;
import com.ram.instalaciones.SinPermisoException;
import com.ram.instalaciones.dominio.Abonado;
import com.ram.instalaciones.dominio.FichaDeInstalacion;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioInstalacionesTest {

    private final Map<UUID, FichaDeInstalacion> instalaciones = new LinkedHashMap<>();
    private final Map<UUID, Abonado> abonados = new LinkedHashMap<>();
    private final Set<String> pagos = new HashSet<>();
    private final Map<ClienteId, DatosCliente> fichas = new LinkedHashMap<>();
    private UsuarioAutenticado logueado = personal(Rol.TECNICO);
    private ServicioInstalaciones servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioInstalaciones(repositorio(), clientes(), () -> Optional.ofNullable(logueado),
                Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void seRegistraConClienteNuevo_numeroCorrelativo_yEmpiezaEnVisita() {
        Instalacion primera = servicio.crear(datos(null));
        Instalacion segunda = servicio.crear(new DatosDeInstalacion(primera.clienteId(), null, "Oroño 500", EstadoInstalacion.PRESUPUESTO,
                null, null, null, null, null, null));

        assertThat(primera.numero()).isEqualTo(1);
        assertThat(segunda.numero()).isEqualTo(2);
        assertThat(primera.estado()).isEqualTo(EstadoInstalacion.VISITA);
        assertThat(primera.cliente()).isEqualTo("Carla Ruiz");
        assertThat(fichas).hasSize(1); // la segunda reusa la ficha
    }

    @Test
    void alTerminarSaleDelListadoEnCurso() {
        Instalacion i = servicio.crear(datos(null));
        servicio.editar(i.id(), datos(EstadoInstalacion.TERMINADA));

        assertThat(servicio.listar(false)).isEmpty();
        assertThat(servicio.listar(true)).hasSize(1);
        assertThat(servicio.obtener(i.id()).orElseThrow().camaras()).isEqualTo(4);
    }

    @Test
    void validaLosDatos() {
        assertThatThrownBy(() -> servicio.crear(new DatosDeInstalacion(null, new DatosDeInstalacion.ClienteNuevo("Ana", "3415550000"),
                "  ", null, null, null, null, null, null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.crear(new DatosDeInstalacion(null, null, "Calle 1", null, null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void laPlanillaDeMonitoreoMarcaYDesmarcaMeses() {
        UUID abonado = servicio.agregarAbonado(new DatosDeAbonado(null, new DatosDeInstalacion.ClienteNuevo("Pedro Sosa", "3415551111"),
                "Mitre 10", 25_000L, true));
        logueado = personal(Rol.MOSTRADOR);

        servicio.marcarPago(abonado, 2026, 1, true);
        servicio.marcarPago(abonado, 2026, 3, true);
        servicio.marcarPago(abonado, 2026, 3, false);

        List<Boolean> meses = servicio.abonos(2026).getFirst().pagos();
        assertThat(meses).hasSize(12);
        assertThat(meses.get(0)).isTrue();
        assertThat(meses.get(2)).isFalse();
        assertThat(servicio.abonos(2025).getFirst().pagos().get(0)).isFalse();
        assertThatThrownBy(() -> servicio.marcarPago(abonado, 2026, 13, true)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elTecnicoNoMarcaPagos() {
        UUID abonado = servicio.agregarAbonado(new DatosDeAbonado(null, new DatosDeInstalacion.ClienteNuevo("Pedro Sosa", "3415551111"),
                null, null, true));
        assertThatThrownBy(() -> servicio.marcarPago(abonado, 2026, 1, true)).isInstanceOf(SinPermisoException.class);
    }

    // ─── Dobles ───────────────────────────────────────────────────────────────────────────────

    private static DatosDeInstalacion datos(EstadoInstalacion estado) {
        return new DatosDeInstalacion(null, new DatosDeInstalacion.ClienteNuevo("Carla Ruiz", "341 555-2222"), "Pellegrini 1200",
                estado, "DVR 8 canales + 4 cámaras", 4, LocalDate.of(2026, 11, 3), "Agustín", 350_000L, null);
    }

    private InstalacionesRepositorio repositorio() {
        return new InstalacionesRepositorio() {
            public void guardar(FichaDeInstalacion i) { instalaciones.put(i.id(), i); }
            public Optional<FichaDeInstalacion> instalacion(UUID id) { return Optional.ofNullable(instalaciones.get(id)); }
            public List<FichaDeInstalacion> instalaciones(boolean terminadas) {
                return instalaciones.values().stream().filter(i -> (i.estado() == EstadoInstalacion.TERMINADA) == terminadas).toList();
            }
            public int ultimoNumero() { return instalaciones.values().stream().mapToInt(FichaDeInstalacion::numero).max().orElse(0); }
            public void guardar(Abonado a) { abonados.put(a.id(), a); }
            public Optional<Abonado> abonado(UUID id) { return Optional.ofNullable(abonados.get(id)); }
            public List<Abonado> abonados() { return List.copyOf(abonados.values()); }
            public Set<String> pagos(int anio) {
                Set<String> delAnio = new HashSet<>();
                pagos.stream().filter(p -> p.startsWith(anio + "/")).forEach(p -> delAnio.add(p.substring(5)));
                return delAnio;
            }
            public void marcarPago(UUID abonado, int anio, int mes, boolean pagado) {
                String clave = anio + "/" + abonado + ":" + mes;
                if (pagado) pagos.add(clave); else pagos.remove(clave);
            }
        };
    }

    /** Solo registrar, obtener y obtenerVarios: lo que usa este módulo. */
    @SuppressWarnings("unchecked")
    private Clientes clientes() {
        return (Clientes) Proxy.newProxyInstance(Clientes.class.getClassLoader(), new Class<?>[]{Clientes.class}, (p, m, args) -> switch (m.getName()) {
            case "registrar" -> {
                NuevoCliente n = (NuevoCliente) args[0];
                ClienteId id = ClienteId.nuevo();
                fichas.put(id, new DatosCliente(id, n.nombreCompleto(), n.telefono(), null));
                yield id;
            }
            case "obtener" -> Optional.ofNullable(fichas.get((ClienteId) args[0]));
            case "obtenerVarios" -> ((Collection<ClienteId>) args[0]).stream().map(fichas::get).filter(x -> x != null).toList();
            default -> throw new UnsupportedOperationException(m.getName());
        });
    }

    private static UsuarioAutenticado personal(Rol rol) {
        return new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín", TipoDeCuenta.PERSONAL, rol);
    }
}
