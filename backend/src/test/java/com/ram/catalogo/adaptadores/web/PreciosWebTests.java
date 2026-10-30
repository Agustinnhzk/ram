package com.ram.catalogo.adaptadores.web;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los endpoints del catálogo como los usa un navegador: sin login la tienda, con login el sistema.
 * Los datos van en el taller RAM (http://ram.localhost), con slugs al azar para no chocar con otros tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PreciosWebTests {

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
        slug = "kit-" + sufijo;
        jdbc.update("insert into productos (id, taller_id, categoria_id, slug, nombre, condicion, precio) "
                + "values (?, ?, ?, ?, 'Kit de prueba', 'NUEVO', 100000)", producto, ram.valor(), categoria, slug);
    }

    @Test
    void laTiendaVeElCatalogoSinLogin() throws Exception {
        mvc.perform(get(RAM + "/api/publico/productos/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Kit de prueba"))
                .andExpect(jsonPath("$.condicion").value("nuevo"))
                .andExpect(jsonPath("$.precio").value(100000));
        mvc.perform(get(RAM + "/api/publico/productos").param("q", "kit prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", hasItem(slug)));
    }

    @Test
    void sinLoginNoSePuedenVerNiCambiarPrecios() throws Exception {
        mvc.perform(get(RAM + "/api/sistema/precios")).andExpect(status().isUnauthorized());
        mvc.perform(put(RAM + "/api/sistema/precios/" + producto).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elDuenoCambiaUnPrecioYLoVeEnElHistorial() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);

        mvc.perform(put(RAM + "/api/sistema/precios/" + producto).with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 120000}"))
                .andExpect(status().isNoContent());

        mvc.perform(get(RAM + "/api/sistema/precios/" + producto + "/historial").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precioAnterior").value(100000))
                .andExpect(jsonPath("$[0].precioNuevo").value(120000))
                .andExpect(jsonPath("$[0].motivo").value("MANUAL"))
                .andExpect(jsonPath("$[0].usuario").value("Persona de prueba"));
    }

    @Test
    void sinCsrfNoSeCambiaNada() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);

        mvc.perform(put(RAM + "/api/sistema/precios/" + producto).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTecnicoVePreciosPeroNoLosCambia() throws Exception {
        Cookie sesion = loguear(Rol.TECNICO);

        mvc.perform(get(RAM + "/api/sistema/precios").cookie(sesion)).andExpect(status().isOk());
        mvc.perform(put(RAM + "/api/sistema/precios/" + producto).with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("sin_permiso"));
    }

    @Test
    void unPedidoSinPrecioNoBorraElPrecio() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);

        mvc.perform(put(RAM + "/api/sistema/precios/" + producto).with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalle").value("Falta el precio"));
    }

    @Test
    void laVistaPreviaDeUnAumentoMuestraElPrecioNuevo() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);

        mvc.perform(post(RAM + "/api/sistema/precios/aumento/vista-previa").with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"porcentaje\": 8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precios[?(@.id == '" + producto + "')].precioNuevo").value(hasItem(108000)));
    }

    @Test
    void unPorcentajeAbsurdoSeRechaza() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);

        mvc.perform(post(RAM + "/api/sistema/precios/aumento").with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"porcentaje\": 800}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("datos_invalidos"));
    }

    private Cookie loguear(Rol rol) throws Exception {
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
