package com.ram.trabajos.adaptadores.web;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.trabajos.DatosTrabajo;
import com.ram.trabajos.SinPermisoException;
import com.ram.trabajos.TipoDeTrabajo;
import com.ram.trabajos.TrabajoNoEncontradoException;
import com.ram.trabajos.TrabajoNuevo;
import com.ram.trabajos.Trabajos;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Trabajos realizados (ADR-018).
 *
 * <pre>
 * GET    /api/publico/trabajos                       los publicados (forma de "Trabajo" en web/src/lib/api/tipos.ts)
 * GET    /api/publico/trabajos/{slug}                uno publicado
 * GET    /api/publico/fotos/trabajos/{id}?tam=chica  una foto de un trabajo publicado (JPEG); sin tam, la grande
 *
 * GET    /api/sistema/trabajos                       todos
 * GET    /api/sistema/trabajos/barrios               los barrios ya usados (para sugerirlos)
 * GET    /api/sistema/trabajos/{id}
 * POST   /api/sistema/trabajos                       JSON con los datos → {"id": ...}
 * PUT    /api/sistema/trabajos/{id}                  JSON con los datos
 * DELETE /api/sistema/trabajos/{id}                  con sus fotos
 * POST   /api/sistema/trabajos/{id}/fotos            multipart "foto"
 * DELETE /api/sistema/trabajos/{id}/fotos/{foto}
 * PUT    /api/sistema/trabajos/{id}/fotos            {"orden": [id, ...]}
 * GET    /api/sistema/trabajos/fotos/{foto}?tam=chica  cualquier foto (también de los no publicados)
 * </pre>
 *
 * <p>Las fotos de la tienda van bajo /api/publico/fotos/ como las de productos: robots.txt deja que Google las vea.
 */
@RestController
class TrabajosController {

    private static final EnumSet<TipoDeArchivo> IMAGENES = EnumSet.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG);
    /** Una foto nunca cambia (si se reemplaza, es otra con otro id): se puede guardar un año. */
    private static final CacheControl PARA_SIEMPRE = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();

    private final Trabajos trabajos;

    TrabajosController(Trabajos trabajos) {
        this.trabajos = trabajos;
    }

    // ─── Tienda ──────────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/publico/trabajos")
    List<TrabajoPublicoJson> publicados() {
        return trabajos.publicados().stream().map(TrabajoPublicoJson::de).toList();
    }

    @GetMapping("/api/publico/trabajos/{slug}")
    ResponseEntity<TrabajoPublicoJson> publicado(@PathVariable String slug) {
        return trabajos.publicado(slug).map(t -> ResponseEntity.ok(TrabajoPublicoJson.de(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/publico/fotos/trabajos/{id}")
    ResponseEntity<byte[]> fotoPublica(@PathVariable UUID id, @RequestParam(required = false) String tam) {
        return jpeg(trabajos.fotoPublica(id, "chica".equals(tam)), PARA_SIEMPRE);
    }

    // ─── Sistema ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/sistema/trabajos")
    List<TrabajoJson> todos() {
        return trabajos.todos().stream().map(TrabajoJson::de).toList();
    }

    @GetMapping("/api/sistema/trabajos/barrios")
    List<String> barrios() {
        return trabajos.barrios();
    }

    @GetMapping("/api/sistema/trabajos/{id}")
    TrabajoJson obtener(@PathVariable UUID id) {
        return trabajos.obtener(id).map(TrabajoJson::de).orElseThrow(() -> new TrabajoNoEncontradoException(id.toString()));
    }

    @PostMapping("/api/sistema/trabajos")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> crear(@RequestBody DatosJson datos) {
        return Map.of("id", trabajos.crear(datos.aDominio()));
    }

    @PutMapping("/api/sistema/trabajos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable UUID id, @RequestBody DatosJson datos) {
        trabajos.editar(id, datos.aDominio());
    }

    @DeleteMapping("/api/sistema/trabajos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrar(@PathVariable UUID id) {
        trabajos.borrar(id);
    }

    @PostMapping(value = "/api/sistema/trabajos/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> agregarFoto(@PathVariable UUID id, @RequestPart(required = false) MultipartFile foto) throws IOException {
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Elegí una foto");
        }
        ArchivoSubido archivo = ArchivoSubido.revisar(foto.getOriginalFilename(), foto.getBytes(), IMAGENES,
                ArchivoSubido.TAMANO_MAXIMO_FOTO);
        return Map.of("id", trabajos.agregarFoto(id, archivo));
    }

    @DeleteMapping("/api/sistema/trabajos/{id}/fotos/{foto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrarFoto(@PathVariable UUID id, @PathVariable UUID foto) {
        trabajos.borrarFoto(id, foto);
    }

    @PutMapping("/api/sistema/trabajos/{id}/fotos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ordenarFotos(@PathVariable UUID id, @RequestBody OrdenJson orden) {
        trabajos.ordenarFotos(id, orden.orden());
    }

    /** No es pública: puede ser de un trabajo sin publicar. Se cachea solo en el navegador de quien la ve. */
    @GetMapping("/api/sistema/trabajos/fotos/{foto}")
    ResponseEntity<byte[]> foto(@PathVariable UUID foto, @RequestParam(required = false) String tam) {
        return jpeg(trabajos.foto(foto, "chica".equals(tam)), CacheControl.maxAge(Duration.ofDays(30)).cachePrivate());
    }

    private static ResponseEntity<byte[]> jpeg(Optional<byte[]> foto, CacheControl cache) {
        return foto.map(bytes -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(cache).body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    static String urlPublica(UUID foto, boolean chica) {
        return "/api/publico/fotos/trabajos/" + foto + (chica ? "?tam=chica" : "");
    }

    static String urlDelSistema(UUID foto, boolean chica) {
        return "/api/sistema/trabajos/fotos/" + foto + (chica ? "?tam=chica" : "");
    }

    // ─── Errores (solo de este controller) ────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del trabajo"));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ResponseEntity<Map<String, String>> archivoInvalido(ArchivoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "archivo_invalido", "detalle", e.getMessage()));
    }

    @ExceptionHandler(TrabajoNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "trabajo_no_encontrado"));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }

    // ─── JSON ─────────────────────────────────────────────────────────────────────────────────

    /**
     * La forma de "Trabajo" en web/src/lib/api/tipos.ts. Sin el número de orden ni el permiso: son del taller.
     * tipo en minúscula, como el resto de la tienda ("notebook", "camaras").
     */
    record TrabajoPublicoJson(String slug, String titulo, String tipo, String barrio, String barrioClave, boolean aDomicilio,
                              LocalDate fecha, String problema, String diagnostico, String solucion, String imagen,
                              String miniatura, List<String> imagenes) {
        static TrabajoPublicoJson de(DatosTrabajo t) {
            UUID principal = t.fotos().isEmpty() ? null : t.fotos().getFirst();
            return new TrabajoPublicoJson(t.slug(), t.titulo(), t.tipo().name().toLowerCase(Locale.ROOT), t.barrio(),
                    t.barrioClave(), t.aDomicilio(), t.fecha(), t.problema(), t.diagnostico(), t.solucion(),
                    principal == null ? null : urlPublica(principal, false),
                    principal == null ? null : urlPublica(principal, true),
                    t.fotos().stream().map(f -> urlPublica(f, false)).toList());
        }
    }

    record FotoJson(UUID id, String url) {
    }

    record TrabajoJson(UUID id, String slug, TipoDeTrabajo tipo, String titulo, String barrio, boolean aDomicilio, String problema,
                       String diagnostico, String solucion, LocalDate fecha, Integer numeroDeOrden, boolean consentimiento,
                       boolean publicado, String miniatura, List<FotoJson> fotos) {
        static TrabajoJson de(DatosTrabajo t) {
            return new TrabajoJson(t.id(), t.slug(), t.tipo(), t.titulo(), t.barrio(), t.aDomicilio(), t.problema(),
                    t.diagnostico(), t.solucion(), t.fecha(), t.numeroDeOrden(), t.consentimiento(), t.publicado(),
                    t.fotos().isEmpty() ? null : urlDelSistema(t.fotos().getFirst(), true),
                    t.fotos().stream().map(f -> new FotoJson(f, urlDelSistema(f, true))).toList());
        }
    }

    record DatosJson(TipoDeTrabajo tipo, String titulo, String barrio, Boolean aDomicilio, String problema, String diagnostico,
                     String solucion, LocalDate fecha, Integer numeroDeOrden, Boolean consentimiento, Boolean publicado) {
        TrabajoNuevo aDominio() {
            return new TrabajoNuevo(tipo, titulo, barrio, Boolean.TRUE.equals(aDomicilio), problema, diagnostico, solucion, fecha,
                    numeroDeOrden, Boolean.TRUE.equals(consentimiento), Boolean.TRUE.equals(publicado));
        }
    }

    record OrdenJson(List<UUID> orden) {
    }
}
