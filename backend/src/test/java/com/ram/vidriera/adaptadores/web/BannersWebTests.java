package com.ram.vidriera.adaptadores.web;

import com.ram.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Los banners que trae la migración V11 se ven en la tienda, con la forma de "Promocion" de la web. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BannersWebTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void laTiendaVeLosBannersDeRam() throws Exception {
        mvc.perform(get("http://ram.localhost/api/publico/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].imagen").value(hasItem("/promos/reparacion.jpg")))
                .andExpect(jsonPath("$[0].textoAlternativo").exists())
                .andExpect(jsonPath("$[0].enlace").exists());
    }

    @Test
    void sinLoginNoSeVenLosDelSistema() throws Exception {
        mvc.perform(get("http://ram.localhost/api/sistema/banners")).andExpect(status().isUnauthorized());
    }
}
