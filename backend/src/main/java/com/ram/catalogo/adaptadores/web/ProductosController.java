package com.ram.catalogo.adaptadores.web;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.catalogo.Catalogo;
import com.ram.catalogo.Condicion;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.FichaDeProducto;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.TipoComponente;
import com.ram.catalogo.TipoRam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * La carga de productos y categorías en el sistema (ADR-013). Ver: todo el personal. Cambiar: solo el Dueño (403).
 *
 * <pre>
 * GET    /api/sistema/productos                       todos, con lo justo para el listado
 * GET    /api/sistema/productos/{id}                  todo, para el formulario
 * POST   /api/sistema/productos                       {ficha..., "precio": 45000 | null}  → 201 {"id": ...}
 * PUT    /api/sistema/productos/{id}                  {ficha...}  (el precio va por /api/sistema/precios)
 * PUT    /api/sistema/productos/{id}/oferta           {"precio": 39000, "hasta": "2026-11-30"}
 * DELETE /api/sistema/productos/{id}/oferta
 * POST   /api/sistema/productos/{id}/fotos            multipart "foto" (JPG o PNG, hasta 10 MB) → 201 {"id": ...}
 * DELETE /api/sistema/productos/{id}/fotos/{foto}
 * PUT    /api/sistema/productos/{id}/fotos            {"orden": [id, id, ...]}
 * POST   /api/sistema/categorias                      {"nombre": "Redes", "conInstalacion": false}
 * PUT    /api/sistema/categorias/{slug}               {"nombre": ..., "conInstalacion": ...}
 * </pre>
 */
@RestController
class ProductosController {

    private static final EnumSet<TipoDeArchivo> FOTOS = EnumSet.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG);

    private final Catalogo catalogo;

    ProductosController(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/api/sistema/productos")
    List<ResumenJson> listar() {
        return catalogo.listarParaPrecios().stream().map(ResumenJson::de).toList();
    }

    @GetMapping("/api/sistema/productos/{id}")
    DetalleJson obtener(@PathVariable UUID id) {
        ProductoId producto = ProductoId.de(id);
        return catalogo.obtener(producto).map(DetalleJson::de).orElseThrow(() -> new ProductoNoEncontradoException(producto));
    }

    @PostMapping("/api/sistema/productos")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> crear(@RequestBody FichaJson ficha) {
        return Map.of("id", catalogo.crearProducto(ficha.aFicha(), ficha.precio()).valor());
    }

    @PutMapping("/api/sistema/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable UUID id, @RequestBody FichaJson ficha) {
        catalogo.editarProducto(ProductoId.de(id), ficha.aFicha());
    }

    @PutMapping("/api/sistema/productos/{id}/oferta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ponerOferta(@PathVariable UUID id, @RequestBody OfertaJson oferta) {
        if (oferta.precio() == null || oferta.hasta() == null) {
            throw new IllegalArgumentException("Falta el precio de oferta o hasta cuándo dura");
        }
        catalogo.cambiarOferta(ProductoId.de(id), oferta.precio(), oferta.hasta());
    }

    @DeleteMapping("/api/sistema/productos/{id}/oferta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void quitarOferta(@PathVariable UUID id) {
        catalogo.cambiarOferta(ProductoId.de(id), null, null);
    }

    @PostMapping(value = "/api/sistema/productos/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, Object>> agregarFoto(@PathVariable UUID id, @RequestPart(required = false) MultipartFile foto)
            throws IOException {
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Elegí una foto");
        }
        ArchivoSubido archivo = ArchivoSubido.revisar(foto.getOriginalFilename(), foto.getBytes(), FOTOS, ArchivoSubido.TAMANO_MAXIMO_FOTO);
        UUID nueva = catalogo.agregarFoto(ProductoId.de(id), archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", nueva, "url", CatalogoPublicoController.urlDeFoto(nueva, true)));
    }

    @DeleteMapping("/api/sistema/productos/{id}/fotos/{foto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrarFoto(@PathVariable UUID id, @PathVariable UUID foto) {
        catalogo.borrarFoto(ProductoId.de(id), foto);
    }

    @PutMapping("/api/sistema/productos/{id}/fotos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ordenarFotos(@PathVariable UUID id, @RequestBody OrdenJson orden) {
        catalogo.ordenarFotos(ProductoId.de(id), orden.orden());
    }

    @PostMapping("/api/sistema/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    DatosCategoria crearCategoria(@RequestBody CategoriaJson categoria) {
        return catalogo.crearCategoria(categoria.nombre(), Boolean.TRUE.equals(categoria.conInstalacion()));
    }

    @PutMapping("/api/sistema/categorias/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editarCategoria(@PathVariable String slug, @RequestBody CategoriaJson categoria) {
        catalogo.editarCategoria(slug, categoria.nombre(), Boolean.TRUE.equals(categoria.conInstalacion()));
    }

    // ─── JSON ─────────────────────────────────────────────────────────────────────────────────

    record CategoriaJson(String nombre, Boolean conInstalacion) {
    }

    record OfertaJson(Long precio, LocalDate hasta) {
    }

    record OrdenJson(List<UUID> orden) {
    }

    /** Los datos técnicos con los nombres de Java (CPU, DDR4, MATX): el sistema es nuestro, no hace falta traducir. */
    record TecnicosJson(TipoComponente tipo, String socket, TipoRam tipoRam, FormatoMother formato,
                        List<FormatoMother> formatosSoportados, Integer consumoW, Integer potenciaW, Boolean graficosIntegrados) {

        DatosTecnicos aDatos() {
            return new DatosTecnicos(tipo, socket, tipoRam, formato, formatosSoportados, consumoW, potenciaW,
                    Boolean.TRUE.equals(graficosIntegrados));
        }

        static TecnicosJson de(DatosTecnicos t) {
            return t == null ? null : new TecnicosJson(t.tipo(), t.socket(), t.tipoRam(), t.formato(), t.formatosSoportados(),
                    t.consumoW(), t.potenciaW(), t.graficosIntegrados());
        }
    }

    /** @param precio solo al crear (después, por la página de precios, que deja historial) */
    record FichaJson(String nombre, String categoria, Condicion condicion, String resumen, String descripcion,
                     List<String> caracteristicas, Integer garantiaMeses, Boolean publicado, TecnicosJson tecnicos, Long precio) {

        FichaDeProducto aFicha() {
            return new FichaDeProducto(nombre, categoria, condicion, resumen, descripcion, caracteristicas, garantiaMeses,
                    Boolean.TRUE.equals(publicado), tecnicos == null ? null : tecnicos.aDatos());
        }
    }

    record CategoriaResumen(String slug, String nombre) {
    }

    record ResumenJson(UUID id, String slug, String nombre, CategoriaResumen categoria, Long precio, Long precioVigente,
                       boolean enOferta, LocalDate ofertaHasta, boolean publicado, TipoComponente tipoComponente,
                       String miniatura) {
        static ResumenJson de(DatosProducto p) {
            List<String> chicas = CatalogoPublicoController.imagenesDe(p, true);
            return new ResumenJson(p.id().valor(), p.slug(), p.nombre(),
                    new CategoriaResumen(p.categoria().slug(), p.categoria().nombre()), p.precio(), p.precioVigente(),
                    p.enOferta(), p.ofertaHasta(), p.publicado(), p.esComponente() ? p.tecnicos().tipo() : null,
                    chicas.isEmpty() ? null : chicas.getFirst());
        }
    }

    record FotoJson(UUID id, String url) {
    }

    record DetalleJson(UUID id, String slug, String nombre, String categoria, Condicion condicion, String resumen,
                       String descripcion, List<String> caracteristicas, Integer garantiaMeses, boolean publicado,
                       TecnicosJson tecnicos, Long precio, Long precioOferta, LocalDate ofertaHasta, boolean enOferta,
                       String imagenHeredada, List<FotoJson> fotos) {
        static DetalleJson de(DatosProducto p) {
            return new DetalleJson(p.id().valor(), p.slug(), p.nombre(), p.categoria().slug(), p.condicion(), p.resumen(),
                    p.descripcion(), p.caracteristicas(), p.garantiaMeses(), p.publicado(), TecnicosJson.de(p.tecnicos()),
                    p.precio(), p.precioOferta(), p.ofertaHasta(), p.enOferta(), p.imagen(),
                    p.fotos().stream().map(f -> new FotoJson(f, CatalogoPublicoController.urlDeFoto(f, true))).toList());
        }
    }
}
