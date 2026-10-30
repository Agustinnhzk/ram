package com.ram.pedidos;

import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.TipoComponente;
import com.ram.catalogo.TipoRam;

import java.util.List;

/** Productos y componentes de prueba, con los mismos datos técnicos que el catálogo de ejemplo. */
public final class Productos {

    private static final DatosCategoria CAMARAS = new DatosCategoria("camaras", "Cámaras", true);
    private static final DatosCategoria COMPONENTES = new DatosCategoria("componentes", "Componentes", false);

    public static final DatosProducto KIT = producto("kit-4-camaras", "Kit 4 cámaras + DVR", 250_000L);
    public static final DatosProducto SIN_PRECIO = producto("alarma-x", "Alarma X", null);
    /** De lista $300.000, en oferta a $250.000. */
    public static final DatosProducto EN_OFERTA = new DatosProducto(ProductoId.nuevo(), "dvr-oferta", "DVR en oferta", CAMARAS,
            Condicion.NUEVO, 300_000L, 250_000L, java.time.LocalDate.of(2026, 12, 31), 250_000L, null, List.of(), "", "",
            List.of(), null, true, null);

    public static final DatosProducto RYZEN_5600 = componente("ryzen-5-5600", "AMD Ryzen 5 5600", 150_000L,
            new DatosTecnicos(TipoComponente.CPU, "AM4", null, null, null, 65, null, false));
    public static final DatosProducto RYZEN_8600G = componente("ryzen-5-8600g", "AMD Ryzen 5 8600G", 230_000L,
            new DatosTecnicos(TipoComponente.CPU, "AM5", null, null, null, 65, null, true));
    public static final DatosProducto A520M = componente("a520m", "Mother A520M", 90_000L,
            new DatosTecnicos(TipoComponente.MOTHER, "AM4", TipoRam.DDR4, FormatoMother.MATX, null, null, null, false));
    public static final DatosProducto B650 = componente("b650", "Mother B650 ATX", 200_000L,
            new DatosTecnicos(TipoComponente.MOTHER, "AM5", TipoRam.DDR5, FormatoMother.ATX, null, null, null, false));
    public static final DatosProducto DDR4 = componente("ddr4-16", "16 GB DDR4", 40_000L,
            new DatosTecnicos(TipoComponente.RAM, null, TipoRam.DDR4, null, null, null, null, false));
    public static final DatosProducto DDR5 = componente("ddr5-16", "16 GB DDR5", 55_000L,
            new DatosTecnicos(TipoComponente.RAM, null, TipoRam.DDR5, null, null, null, null, false));
    public static final DatosProducto RTX_4060 = componente("rtx-4060", "RTX 4060", 400_000L,
            new DatosTecnicos(TipoComponente.GPU, null, null, null, null, 115, null, false));
    public static final DatosProducto RTX_4090 = componente("rtx-4090", "RTX 4090", 3_000_000L,
            new DatosTecnicos(TipoComponente.GPU, null, null, null, null, 450, null, false));
    public static final DatosProducto NVME = componente("nvme-1tb", "NVMe 1 TB", 70_000L,
            new DatosTecnicos(TipoComponente.ALMACENAMIENTO, null, null, null, null, null, null, false));
    public static final DatosProducto FUENTE_550 = componente("fuente-550", "Fuente 550 W", 60_000L,
            new DatosTecnicos(TipoComponente.FUENTE, null, null, null, null, null, 550, false));
    public static final DatosProducto GABINETE_MATX = componente("gabinete-matx", "Gabinete mATX", 50_000L,
            new DatosTecnicos(TipoComponente.GABINETE, null, null, null, List.of(FormatoMother.MATX), null, null, false));
    public static final DatosProducto GABINETE_SIN_PRECIO = componente("gabinete-atx", "Gabinete ATX", null,
            new DatosTecnicos(TipoComponente.GABINETE, null, null, null, List.of(FormatoMother.ATX, FormatoMother.MATX), null, null, false));

    public static final List<DatosProducto> COMPONENTES_TODOS = List.of(RYZEN_5600, RYZEN_8600G, A520M, B650, DDR4, DDR5,
            RTX_4060, RTX_4090, NVME, FUENTE_550, GABINETE_MATX, GABINETE_SIN_PRECIO);

    private Productos() {
    }

    private static DatosProducto producto(String slug, String nombre, Long precio) {
        return new DatosProducto(ProductoId.nuevo(), slug, nombre, CAMARAS, Condicion.NUEVO, precio, null, "", "",
                List.of(), null, true, null);
    }

    private static DatosProducto componente(String slug, String nombre, Long precio, DatosTecnicos tecnicos) {
        return new DatosProducto(ProductoId.nuevo(), slug, nombre, COMPONENTES, Condicion.NUEVO, precio, null, "", "",
                List.of(), null, false, tecnicos);
    }
}
