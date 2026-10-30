package com.ram.usuarios.adaptadores.web;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La seguridad de punta a punta, como la usaría un navegador: pedidos HTTP reales a la aplicación
 * (sin red), con cookies de sesión guardadas en PostgreSQL y tokens CSRF.
 *
 * <p>MockMvc toma el host de la URL: "http://ram.localhost/..." es el taller RAM.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SeguridadWebTests {

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

    private String correoDueno;

    @BeforeEach
    void crearDuenoEnRam() {
        correoDueno = "dueno-" + UUID.randomUUID() + "@ram.com";
        TallerId ram = contexto.buscarPorSlug("ram").orElseThrow();
        contexto.ejecutarComo(ram, () -> acceso.darDeAltaPersonal("Agustín", correoDueno, CLAVE, Rol.DUENO));
    }

    @Test
    void laTiendaPreguntaQuienEstaLogueadoSinRecibirUnError() throws Exception {
        mvc.perform(get(RAM + "/api/tienda/sesion")).andExpect(status().isNoContent());
    }

    @Test
    void sinSesionElSistemaNoResponde() throws Exception {
        mvc.perform(get(RAM + "/api/sistema/clientes").param("nombre", "ana"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elPersonalIniciaSesionYEntraAlSistema() throws Exception {
        Cookie sesion = iniciarSesionPersonal(RAM, correoDueno, CLAVE);

        mvc.perform(get(RAM + "/api/sistema/clientes").param("nombre", "ana").cookie(sesion))
                .andExpect(status().isOk());
        mvc.perform(get(RAM + "/api/sistema/sesion").cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Agustín"))
                .andExpect(jsonPath("$.rol").value("DUENO"));
    }

    @Test
    void conCredencialesIncorrectasNoEntra() throws Exception {
        mvc.perform(post(RAM + "/api/sistema/sesion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(credenciales(correoDueno, "clave-equivocada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("credenciales_invalidas"));
    }

    @Test
    void sinTokenCsrfElLoginSeRechaza() throws Exception {
        mvc.perform(post(RAM + "/api/sistema/sesion")
                        .contentType(MediaType.APPLICATION_JSON).content(credenciales(correoDueno, CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unClienteDeLaTiendaNoEntraAlSistema() throws Exception {
        MvcResult registro = mvc.perform(post(RAM + "/api/tienda/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana López","correo":"ana-%s@mail.com","clave":"clave-de-ana"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie sesionCliente = cookieDeSesion(registro);

        mvc.perform(get(RAM + "/api/tienda/sesion").cookie(sesionCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CLIENTE"));
        mvc.perform(get(RAM + "/api/sistema/clientes").param("nombre", "ana").cookie(sesionCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void unaSesionNoSirveEnOtroTaller() throws Exception {
        Cookie sesionDeRam = iniciarSesionPersonal(RAM, correoDueno, CLAVE);
        String otroTaller = "http://" + crearTaller() + ".localhost";

        mvc.perform(get(otroTaller + "/api/sistema/clientes").param("nombre", "ana").cookie(sesionDeRam))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("sesion_de_otro_taller"));
    }

    @Test
    void despuesDeCerrarSesionLaCookieYaNoSirve() throws Exception {
        Cookie sesion = iniciarSesionPersonal(RAM, correoDueno, CLAVE);

        mvc.perform(delete(RAM + "/api/sistema/sesion").with(csrf()).cookie(sesion))
                .andExpect(status().isNoContent());
        mvc.perform(get(RAM + "/api/sistema/sesion").cookie(sesion))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elFrontendPuedePedirElTokenCsrf() throws Exception {
        // No se verifica el nombre del header: en los tests, .with(csrf()) reemplaza el repositorio
        // de tokens por uno de prueba y cambia ese nombre. El valor real (X-XSRF-TOKEN) se ve con curl.
        mvc.perform(get(RAM + "/api/publico/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    private Cookie iniciarSesionPersonal(String taller, String correo, String clave) throws Exception {
        MvcResult resultado = mvc.perform(post(taller + "/api/sistema/sesion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(credenciales(correo, clave)))
                .andExpect(status().isOk())
                .andReturn();
        return cookieDeSesion(resultado);
    }

    /** Spring Session manda la sesión en la cookie "SESSION". */
    private static Cookie cookieDeSesion(MvcResult resultado) {
        Cookie cookie = resultado.getResponse().getCookie("SESSION");
        assertThat(cookie).as("cookie de sesión").isNotNull();
        return cookie;
    }

    private static String credenciales(String correo, String clave) {
        return """
                {"correo":"%s","clave":"%s"}
                """.formatted(correo, clave);
    }

    private String crearTaller() {
        String slug = "otro" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("insert into talleres (id, nombre, slug, estado) values (?, ?, ?, 'ACTIVO')",
                UUID.randomUUID(), "Otro taller", slug);
        return slug;
    }
}
