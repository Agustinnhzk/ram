package com.ram.trabajos.adaptadores.web;

import com.ram.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La migración V15 corre, la tienda puede listar los trabajos y el sistema pide login. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TrabajosWebTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void laTiendaVeLaListaDeTrabajos() throws Exception {
        mvc.perform(get("http://ram.localhost/api/publico/trabajos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void unTrabajoQueNoExisteDa404() throws Exception {
        mvc.perform(get("http://ram.localhost/api/publico/trabajos/no-existe")).andExpect(status().isNotFound());
        mvc.perform(get("http://ram.localhost/api/publico/fotos/trabajos/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinLoginNoSeVenLosDelSistema() throws Exception {
        mvc.perform(get("http://ram.localhost/api/sistema/trabajos")).andExpect(status().isUnauthorized());
        mvc.perform(get("http://ram.localhost/api/sistema/trabajos/barrios")).andExpect(status().isUnauthorized());
    }
}
