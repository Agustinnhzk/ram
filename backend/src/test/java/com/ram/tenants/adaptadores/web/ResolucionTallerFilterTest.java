package com.ram.tenants.adaptadores.web;

import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import com.ram.tenants.TallerId;
import com.ram.tenants.aplicacion.ResolverTaller;
import com.ram.tenants.aplicacion.TalleresEnMemoria;
import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test del filtro sin levantar Spring: con request y response simuladas.
 */
class ResolucionTallerFilterTest {

    private ResolucionTallerFilter filtro;
    private Taller ram;

    @BeforeEach
    void preparar() {
        TalleresEnMemoria talleres = new TalleresEnMemoria();
        ram = talleres.guardar(Taller.crear("RAM", Slug.de("ram")));
        filtro = new ResolucionTallerFilter(new ResolverTaller(talleres, Dominio.de("localhost")));
    }

    @Test
    void dejaElTallerEnElContextoDuranteLaRequestYLoLimpiaAlFinal() throws Exception {
        AtomicReference<Optional<TallerId>> vistoDurante = new AtomicReference<>();
        FilterChain cadena = (req, res) -> vistoDurante.set(ContextoTaller.actual());

        filtro.doFilter(request("ram.localhost"), new MockHttpServletResponse(), cadena);

        assertThat(vistoDurante.get()).contains(ram.id());
        assertThat(ContextoTaller.actual()).isEmpty();
    }

    @Test
    void respondeNotFoundYNoSigueSiElTallerNoExiste() throws Exception {
        AtomicBoolean siguio = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request("desconocido.localhost"), response, (req, res) -> siguio.set(true));

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).contains("taller_no_encontrado");
        assertThat(siguio).isFalse();
    }

    @Test
    void respondeForbiddenSiElTallerEstaSuspendido() throws Exception {
        ram.suspender();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request("ram.localhost"), response, (req, res) -> { });

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void limpiaElContextoAunqueLaRequestFalle() {
        FilterChain cadenaQueFalla = (req, res) -> {
            throw new IllegalStateException("error en el controlador");
        };

        try {
            filtro.doFilter(request("ram.localhost"), new MockHttpServletResponse(), cadenaQueFalla);
        } catch (Exception esperado) {
            // la excepción sigue su camino; lo que importa es que el contexto quede limpio
        }

        assertThat(ContextoTaller.actual()).isEmpty();
    }

    private static MockHttpServletRequest request(String host) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/publico/taller");
        request.setServerName(host);
        return request;
    }
}
