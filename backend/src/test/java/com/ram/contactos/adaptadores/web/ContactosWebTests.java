package com.ram.contactos.adaptadores.web;

import com.ram.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Trabajá con nosotros" por HTTP. En los tests no hay servidor de correo: los mails se escriben en el log
 * (CorreosSoloEnElRegistro), así que "202" quiere decir que el mail se armó y se "mandó".
 * Cada test usa otra IP (with remoteAddr) para no chocar con el límite de envíos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ContactosWebTests {

    private static final String RAM = "http://ram.localhost";
    private static final byte[] PDF = "%PDF-1.7 cv".getBytes();

    @Autowired
    private MockMvc mvc;

    @Test
    void unaPostulacionConCvEnPdf_sinLoginNiCsrf() throws Exception {
        mvc.perform(postulacion("10.0.0.1", new MockMultipartFile("cv", "CV Ana.pdf", "application/pdf", PDF)))
                .andExpect(status().isAccepted());
    }

    @Test
    void unCvQueNoEsPdfNiWordSeRechaza() throws Exception {
        mvc.perform(postulacion("10.0.0.2", new MockMultipartFile("cv", "cv.pdf", "application/pdf", new byte[]{'M', 'Z', 0})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("archivo_invalido"));
    }

    @Test
    void unaEmpresa() throws Exception {
        mvc.perform(post(RAM + "/api/publico/contactos/empresa").with(r -> { r.setRemoteAddr("10.0.0.3"); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa": "Estudio Pérez", "contacto": "Juan", "correo": "juan@estudio.com",
                                 "telefono": "3415550000", "motivo": "ALIANZA_COMERCIAL", "mensaje": "Hola"}
                                """))
                .andExpect(status().isAccepted());
    }

    @Test
    void unMotivoInventadoEsUnError400() throws Exception {
        mvc.perform(post(RAM + "/api/publico/contactos/empresa").with(r -> { r.setRemoteAddr("10.0.0.4"); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresa\": \"X\", \"motivo\": \"HACKEAR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void despuesDeCincoEnviosDesdeLaMismaIpFrena() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(postulacion("10.0.0.5", new MockMultipartFile("cv", "cv.pdf", "application/pdf", PDF)))
                    .andExpect(status().isAccepted());
        }
        mvc.perform(postulacion("10.0.0.5", new MockMultipartFile("cv", "cv.pdf", "application/pdf", PDF)))
                .andExpect(status().isTooManyRequests());
    }

    private static MockMultipartHttpServletRequestBuilder postulacion(String ip, MockMultipartFile cv) {
        MockMultipartHttpServletRequestBuilder pedido = multipart(RAM + "/api/publico/contactos/postulacion").file(cv);
        pedido.param("nombre", "Ana López").param("correo", "ana@mail.com").param("telefono", "341 555-1234")
                .with(r -> { r.setRemoteAddr(ip); return r; });
        return pedido;
    }
}
