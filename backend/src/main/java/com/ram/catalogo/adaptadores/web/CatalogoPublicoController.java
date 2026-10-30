package com.ram.catalogo.adaptadores.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ram.catalogo.Catalogo;
import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.FormatoMother;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * El catálogo para la tienda. Sin login (ADR-003). El JSON tiene EXACTAMENTE la forma de
 * web/src/lib/api/tipos.ts: el día que la web deje los datos de ejemplo, no cambia ninguna pantalla.
 *
 * <pre>
 * GET /api/publico/categorias
 * GET /api/publico/productos?categoria=camaras&condicion=nuevo&q=dvr   (todo opcional)
 * GET /api/publico/productos/{slug}
 * GET /api/publico/componentes                                        (Armá tu PC)
 * GET /api/publico/fotos/{id}?tam=chica                               una foto (JPEG); sin tam, la grande
 * </pre>
 *
 * <p>"precio" es lo que se cobra HOY (la oferta, si hay). Si está en oferta, "precioAnterior" es el de lista (para tacharlo).
 */
@RestController
@RequestMapping("/api/publico")
class CatalogoPublicoController {

    private final Catalogo catalogo;

    CatalogoPublicoController(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/categorias")
    List<CategoriaJson> categorias() {
        return catalogo.categorias().stream().map(CategoriaJson::de).toList();
    }

    @GetMapping("/productos")
    List<ProductoJson> productos(@RequestParam(required = false) String categoria,
                                 @RequestParam(required = false) String condicion,
                                 @RequestParam(required = false) String q) {
        FiltroCatalogo filtro = new FiltroCatalogo(vacioANull(categoria), condicion(condicion), q);
        return catalogo.buscar(filtro).stream().map(ProductoJson::de).toList();
    }

    @GetMapping("/productos/{slug}")
    ResponseEntity<ProductoJson> producto(@PathVariable String slug) {
        return ResponseEntity.of(catalogo.obtenerPublicado(slug).map(ProductoJson::de));
    }

    @GetMapping("/componentes")
    List<ComponenteJson> componentes() {
        return catalogo.componentes().stream().map(ComponenteJson::de).toList();
    }

    /**
     * La foto nunca cambia (si se reemplaza, es otra foto con otro id): el navegador y cualquier intermediario
     * la pueden guardar un año sin volver a pedirla ("immutable").
     */
    @GetMapping("/fotos/{id}")
    ResponseEntity<byte[]> foto(@PathVariable UUID id, @RequestParam(required = false) String tam) {
        return catalogo.foto(id, "chica".equals(tam))
                .map(f -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .body(f.jpeg()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** La URL pública de una foto. Las de la tienda pasan por el proxy de Next, que les agrega el dominio del taller. */
    static String urlDeFoto(UUID foto, boolean chica) {
        return "/api/publico/fotos/" + foto + (chica ? "?tam=chica" : "");
    }

    /** Las fotos subidas; si no hay, la imagen heredada (datos de ejemplo); si tampoco, ninguna. */
    static List<String> imagenesDe(DatosProducto p, boolean chica) {
        if (!p.fotos().isEmpty()) {
            return p.fotos().stream().map(f -> urlDeFoto(f, chica)).toList();
        }
        return p.imagen() == null ? List.of() : List.of(p.imagen());
    }

    /** Igual que la web: un valor que no existe (?condicion=xx) se ignora, como si no estuviera. */
    private static Condicion condicion(String valor) {
        if (valor == null) {
            return null;
        }
        return Arrays.stream(Condicion.values())
                .filter(c -> c.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElse(null);
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static String minusculas(Enum<?> valor) {
        return valor == null ? null : valor.name().toLowerCase(Locale.ROOT);
    }

    /** La web escribe "mATX" (como se ve en las cajas); en Java el enum es MATX. */
    private static String textoDeFormato(FormatoMother formato) {
        return formato == null ? null : (formato == FormatoMother.MATX ? "mATX" : "ATX");
    }

    record CategoriaJson(String slug, String nombre, boolean conInstalacion) {
        static CategoriaJson de(DatosCategoria c) {
            return new CategoriaJson(c.slug(), c.nombre(), c.conInstalacion());
        }
    }

    /**
     * @param imagen     la principal (grande), o null si no tiene ninguna
     * @param miniatura  la principal en chico, para las tarjetas del catálogo
     * @param imagenes   todas, en orden (grandes), para la galería
     */
    record ProductoJson(String id, String slug, String nombre, String categoria, String condicion, Long precio,
                        Long precioAnterior, LocalDate ofertaHasta, String imagen, String miniatura, List<String> imagenes,
                        String resumen, String descripcion, List<String> caracteristicas, Integer garantiaMeses) {
        static ProductoJson de(DatosProducto p) {
            List<String> grandes = imagenesDe(p, false);
            List<String> chicas = imagenesDe(p, true);
            return new ProductoJson(p.id().toString(), p.slug(), p.nombre(), p.categoria().slug(), minusculas(p.condicion()),
                    p.precioVigente(), p.enOferta() ? p.precio() : null, p.enOferta() ? p.ofertaHasta() : null,
                    grandes.isEmpty() ? null : grandes.getFirst(), chicas.isEmpty() ? null : chicas.getFirst(), grandes,
                    p.resumen(), p.descripcion(), p.caracteristicas(), p.garantiaMeses());
        }
    }

    /** NON_NULL: los datos que no corresponden al tipo (el socket de una fuente) no se mandan. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ComponenteJson(String id, String slug, String tipo, String nombre, Long precio, List<String> detalles,
                          String socket, String tipoRam, String formato, List<String> formatosSoportados,
                          Integer consumoW, Integer potenciaW, Boolean graficosIntegrados) {
        static ComponenteJson de(DatosProducto p) {
            DatosTecnicos t = p.tecnicos();
            return new ComponenteJson(p.id().toString(), p.slug(), minusculas(t.tipo()), p.nombre(), p.precioVigente(),
                    p.caracteristicas(), t.socket(), t.tipoRam() == null ? null : t.tipoRam().name(), textoDeFormato(t.formato()),
                    t.formatosSoportados().isEmpty() ? null : t.formatosSoportados().stream().map(CatalogoPublicoController::textoDeFormato).toList(),
                    t.consumoW(), t.potenciaW(), t.graficosIntegrados());
        }
    }
}
