package com.ram.catalogo.adaptadores.web;

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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La carga de productos desde el sistema y cómo se ve en la tienda (ADR-013). Taller RAM. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductosWebTests {

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
    private String categoria;

    @BeforeEach
    void preparar() {
        ram = contexto.buscarPorSlug("ram").orElseThrow();
        categoria = "cat-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("insert into categorias (id, taller_id, slug, nombre) values (?, ?, ?, 'Prueba')", UUID.randomUUID(), ram.valor(), categoria);
    }

    @Test
    void elDuenoCargaUnProductoConFotoYOferta_yLaTiendaLoMuestra() throws Exception {
        Cookie dueno = loguear(Rol.DUENO);
        String nombre = "Cámara de prueba " + UUID.randomUUID().toString().substring(0, 6);
        MvcResult creado = mvc.perform(post(RAM + "/api/sistema/productos").cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "%s", "categoria": "%s", "condicion": "NUEVO", "resumen": "Full HD",
                                 "caracteristicas": ["Wi-Fi"], "garantiaMeses": 6, "publicado": true, "precio": 50000}
                                """.formatted(nombre, categoria)))
                .andExpect(status().isCreated())
                .andReturn();
        String id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

        mvc.perform(multipart(RAM + "/api/sistema/productos/" + id + "/fotos")
                        .file(new MockMultipartFile("foto", "foto.jpg", "image/jpeg", jpg(2400, 1200)))
                        .cookie(dueno).with(csrf()))
                .andExpect(status().isCreated());

        String hasta = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")).plusDays(5).toString();
        mvc.perform(put(RAM + "/api/sistema/productos/" + id + "/oferta").cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\": 42000, \"hasta\": \"%s\"}".formatted(hasta)))
                .andExpect(status().isNoContent());

        String slug = JsonPath.read(mvc.perform(get(RAM + "/api/sistema/productos/" + id).cookie(dueno))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.slug");

        MvcResult publico = mvc.perform(get(RAM + "/api/publico/productos/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(42000))
                .andExpect(jsonPath("$.precioAnterior").value(50000))
                .andExpect(jsonPath("$.ofertaHasta").value(hasta))
                .andExpect(jsonPath("$.imagen", startsWith("/api/publico/fotos/")))
                .andExpect(jsonPath("$.miniatura", containsString("?tam=chica")))
                .andReturn();
        String miniatura = JsonPath.read(publico.getResponse().getContentAsString(), "$.miniatura");

        mvc.perform(get(RAM + miniatura))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Cache-Control", containsString("immutable")));
    }

    @Test
    void elMostradorVePeroNoCarga() throws Exception {
        Cookie mostrador = loguear(Rol.MOSTRADOR);
        mvc.perform(get(RAM + "/api/sistema/productos").cookie(mostrador)).andExpect(status().isOk());
        mvc.perform(post(RAM + "/api/sistema/productos").cookie(mostrador).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"X\", \"categoria\": \"%s\", \"condicion\": \"NUEVO\"}".formatted(categoria)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unaFotoQueNoEsImagenSeRechaza() throws Exception {
        Cookie dueno = loguear(Rol.DUENO);
        String id = JsonPath.read(mvc.perform(post(RAM + "/api/sistema/productos").cookie(dueno).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Y\", \"categoria\": \"%s\", \"condicion\": \"NUEVO\"}".formatted(categoria)))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mvc.perform(multipart(RAM + "/api/sistema/productos/" + id + "/fotos")
                        .file(new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "%PDF-1.4".getBytes()))
                        .cookie(dueno).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("archivo_invalido"));
    }

    @Test
    void elDuenoCreaUnaCategoria() throws Exception {
        String nombre = "Redes " + UUID.randomUUID().toString().substring(0, 6);
        mvc.perform(post(RAM + "/api/sistema/categorias").cookie(loguear(Rol.DUENO)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"%s\"}".formatted(nombre)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug", startsWith("redes-")));
    }

    private static byte[] jpg(int ancho, int alto) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), "jpeg", salida);
        return salida.toByteArray();
    }

    private Cookie loguear(Rol rol) throws Exception {
        String correo = rol.name().toLowerCase() + "-" + UUID.randomUUID() + "@ram.com";
        contexto.ejecutarComo(ram, () -> acceso.darDeAltaPersonal("Persona de prueba", correo, CLAVE, rol));
        return mvc.perform(post(RAM + "/api/sistema/sesion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(correo, CLAVE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("SESSION");
    }
}
