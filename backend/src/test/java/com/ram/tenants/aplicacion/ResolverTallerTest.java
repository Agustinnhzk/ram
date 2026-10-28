package com.ram.tenants.aplicacion;

import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResolverTallerTest {

    private TalleresEnMemoria talleres;
    private ResolverTaller resolver;
    private Taller ram;

    @BeforeEach
    void preparar() {
        talleres = new TalleresEnMemoria();
        resolver = new ResolverTaller(talleres, Dominio.de("plataforma.com.ar"));

        ram = Taller.crear("RAM", Slug.de("ram"));
        ram.agregarDominioPropio(Dominio.de("ramservicios.com.ar"));
        talleres.guardar(ram);
    }

    @Test
    void resuelvePorSubdominioDeLaPlataforma() {
        assertThat(resolver.ejecutar("ram.plataforma.com.ar")).isEqualTo(ram);
    }

    @Test
    void resuelvePorDominioPropio() {
        assertThat(resolver.ejecutar("ramservicios.com.ar")).isEqualTo(ram);
    }

    @Test
    void elSistemaResuelveAlMismoTallerQueLaTienda() {
        assertThat(resolver.ejecutar("sistema.ramservicios.com.ar")).isEqualTo(ram);
        assertThat(resolver.ejecutar("sistema.ram.plataforma.com.ar")).isEqualTo(ram);
    }

    @Test
    void unSubdominioDeLaPlataformaNoSeBuscaComoDominioPropio() {
        // Aunque alguien registre "otro.plataforma.com.ar" como dominio propio,
        // los subdominios de la plataforma se resuelven solo por slug.
        Taller intruso = Taller.crear("Intruso", Slug.de("intruso"));
        intruso.agregarDominioPropio(Dominio.de("otro.plataforma.com.ar"));
        talleres.guardar(intruso);

        assertThatThrownBy(() -> resolver.ejecutar("otro.plataforma.com.ar"))
                .isInstanceOf(TallerNoEncontradoException.class);
    }

    @Test
    void fallaSiElHostNoEsDeNingunTaller() {
        assertThatThrownBy(() -> resolver.ejecutar("desconocido.plataforma.com.ar"))
                .isInstanceOf(TallerNoEncontradoException.class);
        assertThatThrownBy(() -> resolver.ejecutar("otrodominio.com"))
                .isInstanceOf(TallerNoEncontradoException.class);
    }

    @Test
    void fallaSiElHostEsInvalido() {
        assertThatThrownBy(() -> resolver.ejecutar("esto no es un host"))
                .isInstanceOf(TallerNoEncontradoException.class);
        assertThatThrownBy(() -> resolver.ejecutar(null))
                .isInstanceOf(TallerNoEncontradoException.class);
    }

    @Test
    void fallaSiElTallerEstaSuspendido() {
        ram.suspender();
        talleres.guardar(ram);

        assertThatThrownBy(() -> resolver.ejecutar("ramservicios.com.ar"))
                .isInstanceOf(TallerSuspendidoException.class);
    }
}
