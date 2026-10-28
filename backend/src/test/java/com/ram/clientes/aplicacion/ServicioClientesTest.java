package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.NuevoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioClientesTest {

    private ServicioClientes servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioClientes(new ClientesEnMemoria());
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
    void fallaAlActualizarUnClienteQueNoExiste() {
        assertThatThrownBy(() -> servicio.actualizarContacto(ClienteId.nuevo(), "3415551234", null))
                .isInstanceOf(ClienteNoEncontradoException.class);
    }
}
