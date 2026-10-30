package com.ram.catalogo.adaptadores.persistencia;

import com.ram.TestcontainersConfiguration;
import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.TipoComponente;
import com.ram.tenants.TallerId;
import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contra PostgreSQL real: que un taller no vea ni toque el catálogo de otro (ADR-005), y que el
 * mapeo a la base ande de verdad (las listas TEXT[], el historial, los enums de los componentes).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AislamientoCatalogoTests {

    @Autowired
    private Catalogo catalogo;

    @Autowired
    private JdbcTemplate jdbc;

    private TallerId tallerA;
    private TallerId tallerB;
    private UUID productoDeA;

    @BeforeEach
    void preparar() {
        tallerA = crearTaller("a");
        tallerB = crearTaller("b");
        UUID categoriaDeA = crearCategoria(tallerA, "camaras");
        productoDeA = crearProducto(tallerA, categoriaDeA, "kit-secreto", "Kit secreto", 100_000L);
    }

    @AfterEach
    void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unTallerNoVeElCatalogoDeOtro() {
        assertThat(ContextoTaller.con(tallerA, () -> catalogo.buscar(FiltroCatalogo.todo()))).hasSize(1);
        assertThat(ContextoTaller.con(tallerB, () -> catalogo.buscar(FiltroCatalogo.todo()))).isEmpty();
        assertThat(ContextoTaller.con(tallerB, () -> catalogo.obtenerPublicado("kit-secreto"))).isEmpty();
        assertThat(ContextoTaller.con(tallerB, catalogo::categorias)).isEmpty();
    }

    @Test
    void unTallerNoPuedeCambiarElPrecioDeOtroAunqueConozcaElId() {
        loguearDuenoDe(tallerB);

        assertThatThrownBy(() -> ContextoTaller.con(tallerB, () -> {
            catalogo.cambiarPrecio(ProductoId.de(productoDeA), 1L);
            return null;
        })).isInstanceOf(ProductoNoEncontradoException.class);

        Long precio = jdbc.queryForObject("select precio from productos where id = ?", Long.class, productoDeA);
        assertThat(precio).isEqualTo(100_000L);
    }

    @Test
    void elCambioDePrecioYSuHistorialSeGuardanEnLaBase() {
        loguearDuenoDe(tallerA);

        ContextoTaller.con(tallerA, () -> {
            catalogo.cambiarPrecio(ProductoId.de(productoDeA), 120_000L);
            return catalogo.aplicarAumento(new BigDecimal("10"), null);
        });

        assertThat(ContextoTaller.con(tallerA, () -> catalogo.obtenerPublicado("kit-secreto")))
                .get().extracting(DatosProducto::precio).isEqualTo(132_000L);
        assertThat(ContextoTaller.con(tallerA, () -> catalogo.historialDePrecios(ProductoId.de(productoDeA))))
                .extracting(DatosCambioDePrecio::precioNuevo)
                .containsExactly(132_000L, 120_000L);
        // El historial también queda guardado con el taller correcto (Hibernate completó el @TenantId).
        Integer delTallerA = jdbc.queryForObject(
                "select count(*) from historial_precios where producto_id = ? and taller_id = ?",
                Integer.class, productoDeA, tallerA.valor());
        assertThat(delTallerA).isEqualTo(2);
    }

    @Test
    void leeLasListasYLosDatosTecnicosDeLosComponentes() {
        UUID categoria = crearCategoria(tallerA, "componentes");
        jdbc.update("""
                insert into productos (id, taller_id, categoria_id, slug, nombre, condicion, precio, caracteristicas,
                                       tipo_componente, socket, tipo_ram, formato, formatos_soportados, publicado)
                values (?, ?, ?, 'b650', 'Mother B650', 'NUEVO', 260000, ARRAY['Socket AM5', 'DDR5'],
                        'MOTHER', 'AM5', 'DDR5', 'ATX', ARRAY['ATX', 'MATX'], false)
                """, UUID.randomUUID(), tallerA.valor(), categoria);

        List<DatosProducto> componentes = ContextoTaller.con(tallerA, catalogo::componentes);

        assertThat(componentes).singleElement().satisfies(c -> {
            assertThat(c.caracteristicas()).containsExactly("Socket AM5", "DDR5");
            assertThat(c.tecnicos().tipo()).isEqualTo(TipoComponente.MOTHER);
            assertThat(c.tecnicos().socket()).isEqualTo("AM5");
            assertThat(c.tecnicos().formatosSoportados()).containsExactly(FormatoMother.ATX, FormatoMother.MATX);
        });
    }

    // ─── Ayudas: datos por SQL, para no depender de otros módulos por dentro ──────────────────────

    /** Lo que haría el login: dejar al usuario en la sesión de Spring Security. */
    private static void loguearDuenoDe(TallerId taller) {
        UsuarioAutenticado dueno = new UsuarioAutenticado(UUID.randomUUID(), taller.valor(), "Dueño", TipoDeCuenta.PERSONAL, Rol.DUENO);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(dueno, null, List.of()));
    }

    private TallerId crearTaller(String prefijo) {
        UUID id = UUID.randomUUID();
        String slug = prefijo + id.toString().substring(0, 8);
        jdbc.update("insert into talleres (id, nombre, slug, estado) values (?, ?, ?, 'ACTIVO')", id, "Taller " + slug, slug);
        return TallerId.de(id);
    }

    private UUID crearCategoria(TallerId taller, String slug) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into categorias (id, taller_id, slug, nombre) values (?, ?, ?, ?)", id, taller.valor(), slug, slug);
        return id;
    }

    private UUID crearProducto(TallerId taller, UUID categoria, String slug, String nombre, Long precio) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into productos (id, taller_id, categoria_id, slug, nombre, condicion, precio) values (?, ?, ?, ?, ?, 'NUEVO', ?)",
                id, taller.valor(), categoria, slug, nombre, precio);
        return id;
    }
}
