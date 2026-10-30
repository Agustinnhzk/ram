package com.ram.pedidos.dominio;

import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.TipoComponente;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.ram.pedidos.Productos.A520M;
import static com.ram.pedidos.Productos.B650;
import static com.ram.pedidos.Productos.DDR4;
import static com.ram.pedidos.Productos.DDR5;
import static com.ram.pedidos.Productos.FUENTE_550;
import static com.ram.pedidos.Productos.GABINETE_MATX;
import static com.ram.pedidos.Productos.NVME;
import static com.ram.pedidos.Productos.RTX_4060;
import static com.ram.pedidos.Productos.RTX_4090;
import static com.ram.pedidos.Productos.RYZEN_5600;
import static com.ram.pedidos.Productos.RYZEN_8600G;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Las mismas reglas que web/src/lib/arma-tu-pc.ts. Si cambia una, cambia en los dos lados. */
class ArmadoDePcTest {

    private static Map<TipoComponente, DatosProducto> pc(DatosProducto... piezas) {
        Map<TipoComponente, DatosProducto> mapa = new EnumMap<>(TipoComponente.class);
        for (DatosProducto p : piezas) {
            mapa.put(p.tecnicos().tipo(), p);
        }
        return mapa;
    }

    private static final DatosProducto[] COMPLETA = {RYZEN_5600, A520M, DDR4, RTX_4060, NVME, FUENTE_550, GABINETE_MATX};

    @Test
    void unaPcCompletaYCompatible() {
        ArmadoDePc armado = ArmadoDePc.revisar(pc(COMPLETA), false);

        assertThat(armado.precio()).isEqualTo(150_000L + 90_000 + 40_000 + 400_000 + 70_000 + 60_000 + 50_000);
        assertThat(armado.detalle()).containsExactly(
                "Procesador: AMD Ryzen 5 5600", "Mother: Mother A520M", "Memoria RAM: 16 GB DDR4", "Placa de video: RTX 4060",
                "Almacenamiento: NVMe 1 TB", "Fuente: Fuente 550 W", "Gabinete: Gabinete mATX");
        assertThat(armado.enlace()).isEqualTo("/arma-tu-pc?paso=resumen&cpu=ryzen-5-5600&mother=a520m&ram=ddr4-16"
                + "&gpu=rtx-4060&almacenamiento=nvme-1tb&fuente=fuente-550&gabinete=gabinete-matx");
    }

    @Test
    void laFuenteRecomendada_comoEnLaTienda() {
        // (65 + 115 + 50) × 1,5 = 345 → mínimo 450
        assertThat(ArmadoDePc.revisar(pc(COMPLETA), false).potenciaRecomendada()).isEqualTo(450);
        // (65 + 450 + 50) × 1,5 = 847,5 → 850 W: la de 550 no alcanza
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(RYZEN_5600, A520M, DDR4, RTX_4090, NVME, FUENTE_550, GABINETE_MATX), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("850 W");
    }

    @Test
    void detectaCadaIncompatibilidad() {
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(RYZEN_8600G, A520M, DDR4, RTX_4060, NVME, FUENTE_550, GABINETE_MATX), false))
                .hasMessageContaining("socket");
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(RYZEN_5600, A520M, DDR5, RTX_4060, NVME, FUENTE_550, GABINETE_MATX), false))
                .hasMessageContaining("memoria");
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(RYZEN_8600G, B650, DDR5, RTX_4060, NVME, FUENTE_550, GABINETE_MATX), false))
                .hasMessageContaining("gabinete");
    }

    @Test
    void cualquierPasoSeSaltea_yElDetalleLoAclara() {
        ArmadoDePc sinPlaca = ArmadoDePc.revisar(pc(RYZEN_5600, A520M, DDR4, NVME, FUENTE_550, GABINETE_MATX), false);
        assertThat(sinPlaca.detalle()).contains("Placa de video: no incluida");
        assertThat(sinPlaca.enlace()).contains("&gpu=no&");

        ArmadoDePc soloMemoriaYDisco = ArmadoDePc.revisar(pc(DDR4, NVME), false);
        assertThat(soloMemoriaYDisco.precio()).isEqualTo(40_000L + 70_000L);
        assertThat(soloMemoriaYDisco.enlace()).isEqualTo("/arma-tu-pc?paso=resumen&cpu=no&mother=no&ram=ddr4-16"
                + "&gpu=no&almacenamiento=nvme-1tb&fuente=no&gabinete=no");
    }

    @Test
    void sinProcesadorIgualSeRevisaLoQueSiEsta() {
        // Mother DDR4 con memoria DDR5: no van, aunque falte el procesador.
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(A520M, DDR5), false)).hasMessageContaining("memoria");
        // Sin procesador no hay socket con qué comparar: la mother sola está bien.
        assertThat(ArmadoDePc.revisar(pc(B650, DDR5), false).precio()).isEqualTo(200_000L + 55_000L);
    }

    @Test
    void conGraficosIntegradosNoHaceFaltaPlaca() {
        // Una mother AM5 mATX no está en los datos de prueba: se arma una a medida con la B650 y un gabinete que acepta ATX.
        ArmadoDePc armado = ArmadoDePc.revisar(pc(RYZEN_8600G, B650, DDR5, NVME, FUENTE_550,
                com.ram.pedidos.Productos.GABINETE_SIN_PRECIO), true);

        assertThat(armado.detalle()).contains("Placa de video: gráficos integrados del procesador");
        assertThat(armado.enlace()).contains("&gpu=integrada&");
        assertThat(armado.precio()).as("el gabinete no tiene precio").isNull();
        assertThat(armado.piezasSinPrecio()).containsExactly("Gabinete ATX");
    }

    @Test
    void hayQueElegirAlMenosUnaPieza() {
        assertThatThrownBy(() -> ArmadoDePc.revisar(pc(), false)).hasMessageContaining("al menos una pieza");
        assertThat(ArmadoDePc.revisar(pc(RYZEN_5600), false).precio()).isEqualTo(150_000L);
    }

    @Test
    void usaElPrecioVigente_siUnComponenteEstaEnOferta() {
        DatosProducto enOferta = new DatosProducto(RYZEN_5600.id(), RYZEN_5600.slug(), RYZEN_5600.nombre(), RYZEN_5600.categoria(),
                RYZEN_5600.condicion(), 150_000L, 120_000L, java.time.LocalDate.of(2026, 12, 31), 120_000L, null, List.of(),
                "", "", List.of(), null, false, RYZEN_5600.tecnicos());
        assertThat(ArmadoDePc.revisar(pc(enOferta, A520M), false).precio()).isEqualTo(120_000L + 90_000L);
    }

    @Test
    void unaPiezaEnElLugarEquivocadoSeRechaza() {
        Map<TipoComponente, DatosProducto> piezas = pc(COMPLETA);
        piezas.put(TipoComponente.GPU, NVME);

        assertThatThrownBy(() -> ArmadoDePc.revisar(piezas, false))
                .hasMessageContaining("no es un componente de tipo Placa de video");
        assertThat(List.of(COMPLETA)).hasSize(7);
    }
}
