package com.ram.tenants.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DominioTest {

    private final Dominio plataforma = Dominio.de("plataforma.com.ar");

    @Test
    void normalizaMayusculasPuertoYPuntoFinal() {
        assertThat(Dominio.de(" RAM.Localhost:8080 ").valor()).isEqualTo("ram.localhost");
        assertThat(Dominio.de("ramservicios.com.ar.").valor()).isEqualTo("ramservicios.com.ar");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "con espacio.com", "-guion.com", "doble..punto.com", "a_b.com"})
    void rechazaHostsInvalidos(String host) {
        assertThat(Dominio.intentar(host)).isEmpty();
    }

    @Test
    void reconoceUnSubdominioDirectoDeLaPlataforma() {
        assertThat(Dominio.de("ram.plataforma.com.ar").subdominioDe(plataforma))
                .contains(Slug.de("ram"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plataforma.com.ar", "a.b.plataforma.com.ar", "www.plataforma.com.ar",
            "ramservicios.com.ar", "otraplataforma.com.ar"})
    void noDevuelveSlugSiNoEsUnTallerDeLaPlataforma(String host) {
        assertThat(Dominio.de(host).subdominioDe(plataforma)).isEmpty();
    }

    @Test
    void quitaElPrefijoDelSistema() {
        assertThat(Dominio.de("sistema.ramservicios.com.ar").sinPrefijo("sistema."))
                .isEqualTo(Dominio.de("ramservicios.com.ar"));
        assertThat(Dominio.de("ramservicios.com.ar").sinPrefijo("sistema."))
                .isEqualTo(Dominio.de("ramservicios.com.ar"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sistema", "www", "api", "admin"})
    void losSlugsReservadosNoSePuedenUsar(String reservado) {
        assertThatThrownBy(() -> Slug.de(reservado)).isInstanceOf(IllegalArgumentException.class);
    }
}
