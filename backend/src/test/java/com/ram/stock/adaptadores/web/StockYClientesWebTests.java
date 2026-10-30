package com.ram.stock.adaptadores.web;

import com.jayway.jsonpath.JsonPath;
import com.ram.TestcontainersConfiguration;
import com.ram.tenants.ContextoDeTaller;
import com.ram.tenants.TallerId;
import com.ram.usuarios.Rol;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bloque 3 (ADR-014) por HTTP y contra PostgreSQL de verdad: el stock se carga, la tienda lo ve, una compra sin
 * stock queda reservada, confirmar el pago descuenta, y quien compra aparece en la ficha de clientes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class StockYClientesWebTests {

    private static final String RAM = "http://ram.localhost";
    private static final String CLAVE = "una-clave-segura";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ServicioDeAcceso acceso;

    @Autowired
    private ContextoDeTaller contexto;

    @Autowired
    private JdbcTemplate jdbc;

    private TallerId ram;
    private UUID producto;
    private String slug;

    @BeforeEach
    void preparar() {
        ram = contexto.buscarPorSlug("ram").orElseThrow();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        UUID categoria = UUID.randomUUID();
        jdbc.update("insert into categorias (id, taller_id, slug, nombre) values (?, ?, ?, 'Prueba')",
                categoria, ram.valor(), "cat-" + sufijo);
        producto = UUID.randomUUID();
        slug = "dvr-" + sufijo;
        jdbc.update("insert into productos (id, taller_id, categoria_id, slug, nombre, condicion, precio) "
                + "values (?, ?, ?, ?, 'DVR de prueba', 'NUEVO', 100000)", producto, ram.valor(), categoria, slug);
    }

    @Test
    void elStockCompleto_deLaCargaALaReserva() throws Exception {
        Cookie mostrador = loguearPersonal(Rol.MOSTRADOR);

        // Sin control: la tienda no dice nada de este producto.
        mvc.perform(get(RAM + "/api/publico/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == '%s')]".formatted(slug)).isEmpty());

        mvc.perform(post(RAM + "/api/sistema/stock/" + producto).cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 1}"))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/publico/stock"))
                .andExpect(jsonPath("$[?(@.slug == '%s')].estado".formatted(slug)).value(hasItem("ULTIMAS_UNIDADES")));

        // Compra 3 con 1 en stock: la línea queda como reserva.
        Cookie cliente = registrarCliente("Ana López");
        String pedido = crearPedido(cliente, 3);
        mvc.perform(get(RAM + "/api/tienda/pedidos/" + pedido).cookie(cliente))
                .andExpect(jsonPath("$.lineas[0].reserva").value(true));

        mvc.perform(post(RAM + "/api/sistema/pedidos/" + pedido + "/confirmar-pago").cookie(mostrador).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get(RAM + "/api/sistema/stock").cookie(mostrador))
                .andExpect(jsonPath("$[?(@.id == '%s')].cantidad".formatted(producto)).value(hasItem(0)))
                .andExpect(jsonPath("$[?(@.id == '%s')].reservado".formatted(producto)).value(hasItem(2)));
        MvcResult reservas = mvc.perform(get(RAM + "/api/sistema/stock/reservas?pedido=" + pedido).cookie(mostrador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(2))
                .andExpect(jsonPath("$[0].sePuedeCubrir").value(false))
                .andReturn();
        String reserva = JsonPath.read(reservas.getResponse().getContentAsString(), "$[0].id");

        // Llega mercadería y se cubre la reserva.
        mvc.perform(post(RAM + "/api/sistema/stock/" + producto + "/ingresos").cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 5, \"nota\": \"Factura 123\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post(RAM + "/api/sistema/stock/reservas/" + reserva + "/cubrir").cookie(mostrador).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/sistema/stock/" + producto + "/movimientos").cookie(mostrador))
                .andExpect(jsonPath("$[0].tipo").value("RESERVA_CUBIERTA"))
                .andExpect(jsonPath("$[0].saldo").value(3));

        // Se cancela el pedido pagado: vuelven las 3 unidades (1 vendida + 2 cubiertas).
        mvc.perform(post(RAM + "/api/sistema/pedidos/" + pedido + "/cancelar").cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Se arrepintió\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/sistema/stock").cookie(mostrador))
                .andExpect(jsonPath("$[?(@.id == '%s')].cantidad".formatted(producto)).value(hasItem(6)));
    }

    @Test
    void reglasDelStock() throws Exception {
        Cookie tecnico = loguearPersonal(Rol.TECNICO);
        mvc.perform(post(RAM + "/api/sistema/stock/" + producto).cookie(tecnico).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 1}"))
                .andExpect(status().isForbidden());

        Cookie dueno = loguearPersonal(Rol.DUENO);
        mvc.perform(post(RAM + "/api/sistema/stock/" + producto + "/ingresos").cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 1}"))
                .andExpect(status().isConflict());
        mvc.perform(post(RAM + "/api/sistema/stock/" + producto).cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": -1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(RAM + "/api/sistema/stock/" + producto).cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 4}"))
                .andExpect(status().isNoContent());
        mvc.perform(put(RAM + "/api/sistema/stock/" + producto).cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 3}"))
                .andExpect(status().isBadRequest()); // sin motivo
        mvc.perform(delete(RAM + "/api/sistema/stock/" + producto).cookie(dueno).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void quienCompraApareceEnClientes_yVeSusReparacionesEnLaTienda() throws Exception {
        String nombre = "Compradora " + UUID.randomUUID().toString().substring(0, 6);
        Cookie cliente = registrarCliente(nombre);
        String pedido = crearPedido(cliente, 1);

        Cookie mostrador = loguearPersonal(Rol.MOSTRADOR);
        MvcResult encontrados = mvc.perform(get(RAM + "/api/sistema/clientes").param("texto", nombre).cookie(mostrador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCompleto").value(nombre))
                .andExpect(jsonPath("$[0].cuenta.id").isNotEmpty())
                .andReturn();
        String ficha = JsonPath.read(encontrados.getResponse().getContentAsString(), "$[0].id");
        String cuenta = JsonPath.read(encontrados.getResponse().getContentAsString(), "$[0].cuenta.id");

        mvc.perform(get(RAM + "/api/sistema/clientes/" + ficha).cookie(mostrador))
                .andExpect(jsonPath("$.cuenta.correo").isNotEmpty());
        mvc.perform(get(RAM + "/api/sistema/pedidos?cuenta=" + cuenta).cookie(mostrador))
                .andExpect(jsonPath("$[0].id").value(pedido));
        mvc.perform(put(RAM + "/api/sistema/clientes/" + ficha).cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"%s\",\"telefono\":\"3415551234\",\"notas\":\"Prefiere WhatsApp\"}".formatted(nombre)))
                .andExpect(status().isNoContent());

        // Una reparación de esa ficha aparece en la tienda.
        mvc.perform(post(RAM + "/api/sistema/reparaciones").cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero": %d, "clienteId": "%s", "equipo": "Notebook Lenovo", "problema": "No enciende"}
                                """.formatted(800_000 + (int) (Math.random() * 100_000), ficha)))
                .andExpect(status().isCreated());
        mvc.perform(get(RAM + "/api/tienda/reparaciones").cookie(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipo").value("Notebook Lenovo"));
        mvc.perform(get(RAM + "/api/sistema/reparaciones?cliente=" + ficha).cookie(mostrador))
                .andExpect(jsonPath("$[0].equipo").value("Notebook Lenovo"));

        mvc.perform(delete(RAM + "/api/sistema/clientes/" + ficha + "/cuenta").cookie(mostrador).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/tienda/reparaciones").cookie(cliente))
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private String crearPedido(Cookie cliente, int cantidad) throws Exception {
        MvcResult resultado = mvc.perform(post(RAM + "/api/tienda/pedidos").cookie(cliente).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"tipo": "producto", "slug": "%s", "cantidad": %d}], "entrega": "RETIRO",
                                 "telefono": "34155%05d"}
                                """.formatted(slug, cantidad, (int) (Math.random() * 99_999))))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }

    private Cookie registrarCliente(String nombre) throws Exception {
        String correo = "cliente-" + UUID.randomUUID() + "@mail.com";
        MvcResult resultado = mvc.perform(post(RAM + "/api/tienda/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"%s\",\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(nombre, correo, CLAVE)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return resultado.getResponse().getCookie("SESSION");
    }

    private Cookie loguearPersonal(Rol rol) throws Exception {
        String correo = rol.name().toLowerCase() + "-" + UUID.randomUUID() + "@ram.com";
        contexto.ejecutarComo(ram, () -> acceso.darDeAltaPersonal("Persona de prueba", correo, CLAVE, rol));
        MvcResult resultado = mvc.perform(post(RAM + "/api/sistema/sesion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(correo, CLAVE)))
                .andExpect(status().isOk())
                .andReturn();
        return resultado.getResponse().getCookie("SESSION");
    }
}
