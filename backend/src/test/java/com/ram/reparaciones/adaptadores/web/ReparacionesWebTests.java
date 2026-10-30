package com.ram.reparaciones.adaptadores.web;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Las reparaciones por HTTP, como las usan el sistema y la página de seguimiento. Taller RAM. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ReparacionesWebTests {

    private static final String RAM = "http://ram.localhost";
    private static final String CLAVE = "una-clave-segura";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ServicioDeAcceso acceso;

    @Autowired
    private ContextoDeTaller contexto;

    private TallerId ram;
    private int numero;
    private String telefono;

    @BeforeEach
    void preparar() {
        ram = contexto.buscarPorSlug("ram").orElseThrow();
        // Números y teléfonos al azar: los tests comparten la base del taller RAM.
        numero = ThreadLocalRandom.current().nextInt(100_000, 9_000_000);
        telefono = "341" + ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
    }

    @Test
    void elDuenoIngresaUnEquipoYVeElDetalleConElPassword() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);
        String id = ingresar(sesion);

        mvc.perform(get(RAM + "/api/sistema/reparaciones/" + id).cookie(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(numero))
                .andExpect(jsonPath("$.cliente.nombreCompleto").value("Ana López"))
                .andExpect(jsonPath("$.password").value("clave-del-equipo"))
                .andExpect(jsonPath("$.estado").value("INGRESADO"))
                .andExpect(jsonPath("$.siguientes[0]").value("EN_DIAGNOSTICO"));
    }

    @Test
    void elMostradorNoRecibeElPassword() throws Exception {
        String id = ingresar(loguear(Rol.DUENO));

        mvc.perform(get(RAM + "/api/sistema/reparaciones/" + id).cookie(loguear(Rol.MOSTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordVisible").value(false))
                .andExpect(content().string(not(containsString("clave-del-equipo"))));
    }

    @Test
    void unNumeroRepetidoYUnSaltoDeEstadoSeRechazan() throws Exception {
        Cookie sesion = loguear(Rol.DUENO);
        String id = ingresar(sesion);

        mvc.perform(post(RAM + "/api/sistema/reparaciones").with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content(ingresoJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("numero_repetido"));
        mvc.perform(post(RAM + "/api/sistema/reparaciones/" + id + "/estado").with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"LISTO_PARA_RETIRAR\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("transicion_invalida"));
    }

    @Test
    void elClienteSigueSuReparacionSinLoginNiCsrf() throws Exception {
        ingresar(loguear(Rol.DUENO));

        mvc.perform(post(RAM + "/api/publico/seguimiento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\": %d, \"telefono\": \"%s\"}".formatted(numero, telefono.substring(telefono.length() - 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INGRESADO"))
                .andExpect(jsonPath("$.equipo").value("Notebook Lenovo"))
                .andExpect(content().string(not(containsString("clave-del-equipo"))))
                .andExpect(content().string(not(containsString("Rayón"))));
    }

    @Test
    void despuesDeCincoIntentosFallidosLaOrdenSeBloquea() throws Exception {
        ingresar(loguear(Rol.DUENO));
        String equivocado = "{\"numero\": %d, \"telefono\": \"0000\"}".formatted(numero);

        for (int i = 0; i < 5; i++) {
            mvc.perform(post(RAM + "/api/publico/seguimiento").contentType(MediaType.APPLICATION_JSON).content(equivocado))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("no_encontrada"));
        }
        // Ahora ni con los dígitos correctos: hay que esperar 15 minutos.
        String correcto = "{\"numero\": %d, \"telefono\": \"%s\"}".formatted(numero, telefono.substring(telefono.length() - 4));
        mvc.perform(post(RAM + "/api/publico/seguimiento").contentType(MediaType.APPLICATION_JSON).content(correcto))
                .andExpect(status().isTooManyRequests());
    }

    private String ingresar(Cookie sesion) throws Exception {
        MvcResult resultado = mvc.perform(post(RAM + "/api/sistema/reparaciones").with(csrf()).cookie(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content(ingresoJson()))
                .andExpect(status().isCreated())
                .andReturn();
        return resultado.getResponse().getContentAsString().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    private String ingresoJson() {
        return """
                {"numero": %d, "clienteNuevo": {"nombreCompleto": "Ana López", "telefono": "%s"},
                 "equipo": "Notebook Lenovo", "conCargador": true, "password": "clave-del-equipo",
                 "problema": "No prende", "observaciones": "Rayón en la tapa"}
                """.formatted(numero, telefono);
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
