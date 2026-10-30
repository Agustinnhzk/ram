package com.ram.pedidos.adaptadores.web;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La compra completa por HTTP, como la hacen la tienda y el sistema, contra PostgreSQL de verdad:
 * cliente se registra → hace el pedido → sube el comprobante → el taller lo ve, abre el archivo y confirma.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PedidosWebTests {

    private static final String RAM = "http://ram.localhost";
    private static final String CLAVE = "una-clave-segura";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 1, 2, 3};

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ServicioDeAcceso acceso;

    @Autowired
    private ContextoDeTaller contexto;

    @Autowired
    private JdbcTemplate jdbc;

    private TallerId ram;
    private String slug;

    @BeforeEach
    void preparar() {
        ram = contexto.buscarPorSlug("ram").orElseThrow();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        UUID categoria = UUID.randomUUID();
        jdbc.update("insert into categorias (id, taller_id, slug, nombre) values (?, ?, ?, 'Prueba')",
                categoria, ram.valor(), "cat-" + sufijo);
        slug = "kit-" + sufijo;
        jdbc.update("insert into productos (id, taller_id, categoria_id, slug, nombre, condicion, precio) "
                + "values (?, ?, ?, ?, 'Kit de prueba', 'NUEVO', 100000)", UUID.randomUUID(), ram.valor(), categoria, slug);
    }

    @Test
    void laCompraCompleta() throws Exception {
        Cookie cliente = registrarCliente();

        mvc.perform(get(RAM + "/api/tienda/datos-de-pago").cookie(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("ram.nh"))
                .andExpect(jsonPath("$.entidad").value("Naranja X"));

        // El carrito dice que vale $1: el servidor lo ignora y cobra el precio del catálogo.
        String id = crearPedido(cliente, """
                {"items": [{"tipo": "producto", "slug": "%s", "cantidad": 2, "precio": 1}],
                 "entrega": "ENVIO_ROSARIO", "direccion": "Córdoba 1234, Rosario", "telefono": "341 555-1234"}
                """.formatted(slug));

        mvc.perform(get(RAM + "/api/tienda/pedidos/" + id).cookie(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(200000))
                .andExpect(jsonPath("$.estado").value("ESPERANDO_PAGO"))
                .andExpect(jsonPath("$.lineas[0].descripcion").value("Kit de prueba"))
                .andExpect(jsonPath("$.datosDePago.alias").value("ram.nh"))
                .andExpect(jsonPath("$.puedeSubirComprobante").value(true));

        mvc.perform(multipart(RAM + "/api/tienda/pedidos/" + id + "/comprobante")
                        .file(new MockMultipartFile("archivo", "mi pago.png", "image/png", PNG))
                        .cookie(cliente).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get(RAM + "/api/tienda/pedidos").cookie(cliente))
                .andExpect(jsonPath("$[0].estado").value("PAGO_INFORMADO"));

        // El taller
        Cookie dueno = loguearPersonal(Rol.DUENO);
        mvc.perform(get(RAM + "/api/sistema/pedidos?estado=PAGO_INFORMADO").cookie(dueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(id)));

        MvcResult detalle = mvc.perform(get(RAM + "/api/sistema/pedidos/" + id).cookie(dueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprobantes[0].nombre").value("mi pago.png"))
                .andReturn();
        String url = JsonPath.read(detalle.getResponse().getContentAsString(), "$.comprobantes[0].url");

        mvc.perform(get(RAM + url).cookie(dueno))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Content-Disposition", containsString("inline")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(PNG));

        mvc.perform(post(RAM + "/api/sistema/pedidos/" + id + "/confirmar-pago").cookie(dueno).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(post(RAM + "/api/sistema/pedidos/" + id + "/entregar").cookie(dueno).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/tienda/pedidos/" + id).cookie(cliente))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    }

    @Test
    void laCotizacionYElPrecioDelArmado() throws Exception {
        mvc.perform(get(RAM + "/api/publico/armado-de-pc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(30000));

        mvc.perform(post(RAM + "/api/tienda/pedidos/cotizacion").cookie(registrarCliente()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"tipo": "producto", "slug": "%s", "cantidad": 3}],
                                 "entrega": "ENVIO_OTRA_LOCALIDAD", "direccion": "Funes", "telefono": "3415551234"}
                                """.formatted(slug)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(300000))
                .andExpect(jsonPath("$.envioACotizar").value(true));
    }

    @Test
    void elComprobanteTieneQueSerDeVerdadUnaImagenOUnPdf() throws Exception {
        Cookie cliente = registrarCliente();
        String id = crearPedido(cliente, pedidoSimple());

        mvc.perform(multipart(RAM + "/api/tienda/pedidos/" + id + "/comprobante")
                        .file(new MockMultipartFile("archivo", "pago.png", "image/png", new byte[]{'M', 'Z', 0, 0}))
                        .cookie(cliente).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("archivo_invalido"));
    }

    @Test
    void unClienteNoVeElPedidoDeOtro_niUnClienteEntraAlSistema() throws Exception {
        Cookie ana = registrarCliente();
        String id = crearPedido(ana, pedidoSimple());
        Cookie otro = registrarCliente();

        mvc.perform(get(RAM + "/api/tienda/pedidos/" + id).cookie(otro)).andExpect(status().isNotFound());
        mvc.perform(post(RAM + "/api/tienda/pedidos/" + id + "/cancelar").cookie(otro).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(get(RAM + "/api/sistema/pedidos").cookie(ana)).andExpect(status().isForbidden());
    }

    @Test
    void sinLoginNoSeCompra_ySinCsrfTampoco() throws Exception {
        mvc.perform(post(RAM + "/api/tienda/pedidos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSimple()))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(RAM + "/api/tienda/pedidos").cookie(registrarCliente())
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSimple()))
                .andExpect(status().isForbidden());
    }

    @Test
    void unProductoSinPrecioNoSeCompra() throws Exception {
        jdbc.update("update productos set precio = null where slug = ?", slug);

        mvc.perform(post(RAM + "/api/tienda/pedidos").cookie(registrarCliente()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoSimple()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("producto_no_disponible"));
    }

    @Test
    void elTecnicoNoConfirmaPagos() throws Exception {
        String id = crearPedido(registrarCliente(), pedidoSimple());

        mvc.perform(post(RAM + "/api/sistema/pedidos/" + id + "/confirmar-pago").cookie(loguearPersonal(Rol.TECNICO)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    private String pedidoSimple() {
        return """
                {"items": [{"tipo": "producto", "slug": "%s", "cantidad": 1}], "entrega": "RETIRO", "telefono": "3415551234"}
                """.formatted(slug);
    }

    private String crearPedido(Cookie cliente, String json) throws Exception {
        MvcResult resultado = mvc.perform(post(RAM + "/api/tienda/pedidos").cookie(cliente).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }

    private Cookie registrarCliente() throws Exception {
        String correo = "cliente-" + UUID.randomUUID() + "@mail.com";
        MvcResult resultado = mvc.perform(post(RAM + "/api/tienda/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana López\",\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(correo, CLAVE)))
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
