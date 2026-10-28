package com.ram.tenants.adaptadores.persistencia;

import com.ram.TestcontainersConfiguration;
import com.ram.tenants.aplicacion.ResolverTaller;
import com.ram.tenants.aplicacion.TalleresRepositorio;
import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Slug;
import com.ram.tenants.dominio.Taller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración contra un PostgreSQL real en Docker (Testcontainers).
 * Verifica que las migraciones, el mapeo JPA y las consultas funcionan de verdad,
 * algo que los tests con el repositorio en memoria no pueden probar.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PersistenciaTalleresTests {

    @Autowired
    private TalleresRepositorio talleres;

    @Autowired
    private ResolverTaller resolver;

    @Test
    void laMigracionCreaElTallerRam() {
        assertThat(resolver.ejecutar("ram.localhost").nombre()).isEqualTo("RAM");
    }

    @Test
    void guardaYEncuentraUnTallerPorSuDominioPropio() {
        Taller taller = Taller.crear("Taller de prueba", Slug.de("prueba"));
        taller.agregarDominioPropio(Dominio.de("tallerdeprueba.com.ar"));
        talleres.guardar(taller);

        assertThat(talleres.buscarPorDominioPropio(Dominio.de("tallerdeprueba.com.ar")))
                .get()
                .extracting(Taller::slug)
                .isEqualTo(Slug.de("prueba"));
    }
}
