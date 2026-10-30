package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.CuentaYaVinculadaException;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.usuarios.CuentaDeTienda;
import com.ram.usuarios.CuentasDeTienda;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioClientesTest {

    private final Map<UUID, CuentaDeTienda> cuentas = new HashMap<>();
    private final CuentasDeTienda consultaDeCuentas = new CuentasDeTienda() {
        @Override
        public Optional<CuentaDeTienda> porId(UUID id) {
            return Optional.ofNullable(cuentas.get(id));
        }

        @Override
        public Optional<CuentaDeTienda> porCorreo(String correo) {
            return cuentas.values().stream().filter(c -> c.correo().equalsIgnoreCase(correo.strip())).findFirst();
        }

        @Override
        public void borrar(UUID id) {
            cuentas.remove(id);
        }
    };
    private UsuarioAutenticado logueado = new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín",
            TipoDeCuenta.PERSONAL, Rol.DUENO);
    private ServicioClientes servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioClientes(new ClientesEnMemoria(), consultaDeCuentas, () -> Optional.ofNullable(logueado));
    }

    @Test
    void registraUnClienteSinCorreo() {
        ClienteId id = servicio.registrar(new NuevoCliente("Agustín Pérez", "341 555-1234", ""));

        DatosCliente datos = servicio.obtener(id).orElseThrow();
        assertThat(datos.nombreCompleto()).isEqualTo("Agustín Pérez");
        assertThat(datos.telefono()).isEqualTo("3415551234");
        assertThat(datos.correoOpcional()).isEmpty();
    }

    @Test
    void encuentraAlClienteQueVuelveAunqueSeEscribaDistinto() {
        servicio.registrar(new NuevoCliente("Agustín Pérez", "3415551234", null));

        assertThat(servicio.buscarPorNombre("AGUSTIN perez")).hasSize(1);
        assertThat(servicio.buscarPorNombre("perez")).hasSize(1);
    }

    @Test
    void muestraLosHomonimosConSuTelefonoParaDistinguirlos() {
        servicio.registrar(new NuevoCliente("Juan Gómez", "3411111111", null));
        servicio.registrar(new NuevoCliente("Juan Gomez", "3412222222", null));

        assertThat(servicio.buscarPorNombre("juan gomez"))
                .extracting(DatosCliente::telefono)
                .containsExactlyInAnyOrder("3411111111", "3412222222");
    }

    @Test
    void noBuscaConMenosDeDosCaracteres() {
        servicio.registrar(new NuevoCliente("Ana", "3415551234", null));

        assertThat(servicio.buscarPorNombre("a")).isEmpty();
        assertThat(servicio.buscarPorNombre("  ")).isEmpty();
    }

    @Test
    void devuelveComoMaximoVeinteResultados() {
        IntStream.range(0, 25).forEach(i ->
                servicio.registrar(new NuevoCliente("Cliente " + i, "34155500" + (10 + i), null)));

        assertThat(servicio.buscarPorNombre("cliente")).hasSize(ServicioClientes.MAXIMO_RESULTADOS);
    }

    @Test
    void actualizaElContactoYPermiteQuitarElCorreo() {
        ClienteId id = servicio.registrar(new NuevoCliente("Ana López", "3415551234", "ana@mail.com"));

        servicio.actualizarContacto(id, "3419999999", null);

        DatosCliente datos = servicio.obtener(id).orElseThrow();
        assertThat(datos.telefono()).isEqualTo("3419999999");
        assertThat(datos.correoOpcional()).isEmpty();
    }

    @Test
    void traeVariosClientesDeUnaVezYIgnoraLosQueNoExisten() {
        ClienteId ana = servicio.registrar(new NuevoCliente("Ana López", "3415551234", null));
        ClienteId juan = servicio.registrar(new NuevoCliente("Juan Gómez", "3416661234", null));

        assertThat(servicio.obtenerVarios(java.util.List.of(ana, juan, ClienteId.nuevo())))
                .extracting(DatosCliente::nombreCompleto)
                .containsExactlyInAnyOrder("Ana López", "Juan Gómez");
        assertThat(servicio.obtenerVarios(java.util.List.of())).isEmpty();
    }

    @Test
    void fallaAlActualizarUnClienteQueNoExiste() {
        assertThatThrownBy(() -> servicio.actualizarContacto(ClienteId.nuevo(), "3415551234", null))
                .isInstanceOf(ClienteNoEncontradoException.class);
    }

    // ─── Ficha y vínculo con la tienda (ADR-014) ─────────────────────────────────────────────

    @Test
    void laFichaSeEditaConNotasInternas() {
        ClienteId id = servicio.registrar(new NuevoCliente("Juan Gómez", "3415550000", null));

        servicio.editar(id, new DatosDeFicha("Juan Carlos Gómez", "341 555-0001", "JUAN@mail.com", "  Paga siempre en efectivo  "));

        FichaDeCliente ficha = servicio.ficha(id).orElseThrow();
        assertThat(ficha.nombreCompleto()).isEqualTo("Juan Carlos Gómez");
        assertThat(ficha.telefono()).isEqualTo("3415550001");
        assertThat(ficha.correo()).isEqualTo("juan@mail.com");
        assertThat(ficha.notas()).isEqualTo("Paga siempre en efectivo");
        assertThatThrownBy(() -> servicio.editar(id, new DatosDeFicha("", "3415550001", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elListadoBuscaPorNombreOTelefono_ySinTextoMuestraLosUltimos() {
        servicio.registrar(new NuevoCliente("Ana López", "3415551111", null));
        servicio.registrar(new NuevoCliente("Beto Ruiz", "3364222222", null));

        assertThat(servicio.listar(null)).hasSize(2);
        assertThat(servicio.listar("336 42").getFirst().nombreCompleto()).isEqualTo("Beto Ruiz");
        assertThat(servicio.listar("lopez").getFirst().nombreCompleto()).isEqualTo("Ana López");
        assertThat(servicio.listar("34")).isEmpty(); // pocos números: no busca
    }

    @Test
    void alComprarPorPrimeraVezSeCreaLaFichaConLosDatosDeLaCuenta() {
        UUID cuenta = cuenta("Laura Díaz", "laura@mail.com");

        ClienteId id = servicio.vincularAlComprar(cuenta, "341 444-5555").orElseThrow();

        FichaDeCliente ficha = servicio.ficha(id).orElseThrow();
        assertThat(ficha.nombreCompleto()).isEqualTo("Laura Díaz");
        assertThat(ficha.telefono()).isEqualTo("3414445555");
        assertThat(ficha.correo()).isEqualTo("laura@mail.com");
        assertThat(ficha.cuenta().correo()).isEqualTo("laura@mail.com");
        // La segunda compra usa la misma ficha.
        assertThat(servicio.vincularAlComprar(cuenta, "otro teléfono")).contains(id);
        assertThat(servicio.deLaCuenta(cuenta)).contains(id);
    }

    @Test
    void siYaEraClienteDelTallerSeReconocePorCorreoOTelefono() {
        ClienteId porCorreo = servicio.registrar(new NuevoCliente("Laura Díaz", "3410000000", "laura@mail.com"));
        ClienteId porTelefono = servicio.registrar(new NuevoCliente("Pedro Sosa", "3415550000", null));

        assertThat(servicio.vincularAlComprar(cuenta("Lau", "LAURA@mail.com"), "3419999999")).contains(porCorreo);
        assertThat(servicio.vincularAlComprar(cuenta("Pedro", "pedro@mail.com"), "341 555-0000")).contains(porTelefono);
        // Al vincular por teléfono, la ficha toma el correo de la cuenta (no tenía).
        assertThat(servicio.ficha(porTelefono).orElseThrow().correo()).isEqualTo("pedro@mail.com");
    }

    @Test
    void siHayDosFichasConElMismoTelefonoNoAdivina_creaUnaNueva() {
        ClienteId padre = servicio.registrar(new NuevoCliente("Carlos Paz", "3415550000", null));
        ClienteId hijo = servicio.registrar(new NuevoCliente("Carlitos Paz", "3415550000", null));

        ClienteId nueva = servicio.vincularAlComprar(cuenta("Carlos", "c@mail.com"), "3415550000").orElseThrow();

        assertThat(nueva).isNotEqualTo(padre);
        assertThat(nueva).isNotEqualTo(hijo);
    }

    @Test
    void conDatosQueNoSePuedenGuardarNoFalla_simplementeNoVincula() {
        assertThat(servicio.vincularAlComprar(cuenta("Laura", "laura@mail.com"), "abc")).isEmpty();
        assertThat(servicio.vincularAlComprar(UUID.randomUUID(), "3415550000")).isEmpty(); // cuenta que no existe
    }

    @Test
    void laCuentaSeVinculaYDesvinculaAMano_yNoPuedeEstarEnDosFichas() {
        cuenta("Laura Díaz", "laura@mail.com");
        ClienteId una = servicio.registrar(new NuevoCliente("Laura Díaz", "3410000000", null));
        ClienteId otra = servicio.registrar(new NuevoCliente("L. Díaz", "3410000001", null));

        servicio.vincularCuenta(una, "laura@mail.com");
        assertThat(servicio.ficha(una).orElseThrow().cuenta().nombre()).isEqualTo("Laura Díaz");
        assertThatThrownBy(() -> servicio.vincularCuenta(otra, "laura@mail.com")).isInstanceOf(CuentaYaVinculadaException.class);
        assertThatThrownBy(() -> servicio.vincularCuenta(otra, "nadie@mail.com")).isInstanceOf(IllegalArgumentException.class);

        servicio.desvincularCuenta(una);
        assertThat(servicio.ficha(una).orElseThrow().cuenta()).isNull();
        servicio.vincularCuenta(otra, "laura@mail.com");
        assertThat(servicio.ficha(otra).orElseThrow().cuenta()).isNotNull();
    }

    @Test
    void soloElDuenoBorraUnaFicha_yConEllaSuCuentaDeLaTienda() {
        UUID cuenta = cuenta("Laura Díaz", "laura@mail.com");
        ClienteId id = servicio.vincularAlComprar(cuenta, "3414445555").orElseThrow();

        logueado = new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Laura", TipoDeCuenta.PERSONAL, Rol.MOSTRADOR);
        assertThatThrownBy(() -> servicio.borrar(id)).isInstanceOf(com.ram.clientes.SinPermisoException.class);

        logueado = new UsuarioAutenticado(UUID.randomUUID(), UUID.randomUUID(), "Agustín", TipoDeCuenta.PERSONAL, Rol.DUENO);
        servicio.borrar(id);
        assertThat(servicio.ficha(id)).isEmpty();
        assertThat(cuentas).isEmpty();
    }

    private UUID cuenta(String nombre, String correo) {
        UUID id = UUID.randomUUID();
        cuentas.put(id, new CuentaDeTienda(id, nombre, correo.toLowerCase()));
        return id;
    }
}
