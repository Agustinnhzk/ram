package com.ram.tenants.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TallerTest {

    @Test
    void unTallerNuevoEmpiezaActivoYSinDominioPropio() {
        Taller taller = Taller.crear("RAM", Slug.de("ram"));

        assertThat(taller.estaActivo()).isTrue();
        assertThat(taller.dominiosPropios()).isEmpty();
    }

    @Test
    void elNombreSeGuardaSinEspaciosSobrantes() {
        assertThat(Taller.crear("  RAM  ", Slug.de("ram")).nombre()).isEqualTo("RAM");
    }

    @Test
    void noSePuedeCrearSinNombre() {
        assertThatThrownBy(() -> Taller.crear("  ", Slug.de("ram")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sePuedeSuspenderYReactivar() {
        Taller taller = Taller.crear("RAM", Slug.de("ram"));

        taller.suspender();
        assertThat(taller.estaActivo()).isFalse();

        taller.reactivar();
        assertThat(taller.estaActivo()).isTrue();
    }

    @Test
    void losDominiosPropiosNoSePuedenModificarDesdeAfuera() {
        Taller taller = Taller.crear("RAM", Slug.de("ram"));
        taller.agregarDominioPropio(Dominio.de("ramservicios.com.ar"));

        assertThatThrownBy(() -> taller.dominiosPropios().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(taller.dominiosPropios()).containsExactly(Dominio.de("ramservicios.com.ar"));
    }

    @Test
    void dosTalleresSonElMismoSiTienenElMismoId() {
        Taller original = Taller.crear("RAM", Slug.de("ram"));
        Taller renombrado = Taller.reconstruir(original.id(), "RAM Servicios", original.slug(),
                original.estado(), original.dominiosPropios());

        assertThat(renombrado).isEqualTo(original);
    }
}
