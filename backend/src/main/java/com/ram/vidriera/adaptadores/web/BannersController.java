package com.ram.vidriera.adaptadores.web;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.vidriera.BannerNoEncontradoException;
import com.ram.vidriera.BannerNuevo;
import com.ram.vidriera.DatosBanner;
import com.ram.vidriera.SinPermisoException;
import com.ram.vidriera.Vidriera;
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
import java.util.Map;
import java.util.UUID;

/**
 * Banners de la portada (ADR-013).
 *
 * <pre>
 * GET    /api/publico/banners                   los de hoy, con la forma de "Promocion" de la web
 * GET    /api/publico/banners/{id}/imagen       la imagen (JPEG; la URL lleva ?v= para no mostrar una vieja)
 * GET    /api/sistema/banners                   todos
 * POST   /api/sistema/banners                   multipart: imagen + textoAlternativo, enlace, activo, desde?, hasta?
 * PUT    /api/sistema/banners/{id}              JSON con los datos
 * PUT    /api/sistema/banners/{id}/imagen       multipart "imagen"
 * DELETE /api/sistema/banners/{id}
 * PUT    /api/sistema/banners/orden             {"orden": [id, ...]}
 * </pre>
 */
@RestController
class BannersController {

    private static final EnumSet<TipoDeArchivo> IMAGENES = EnumSet.of(TipoDeArchivo.JPG, TipoDeArchivo.PNG);

    private final Vidriera vidriera;

    BannersController(Vidriera vidriera) {
        this.vidriera = vidriera;
    }

    @GetMapping("/api/publico/banners")
    List<PromocionJson> paraLaTienda() {
        return vidriera.paraLaTienda().stream().map(PromocionJson::de).toList();
    }

    @GetMapping("/api/publico/banners/{id}/imagen")
    ResponseEntity<byte[]> imagen(@PathVariable UUID id) {
        return vidriera.imagen(id)
                .map(jpeg -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .body(jpeg))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/sistema/banners")
    List<BannerJson> todos() {
        return vidriera.todos().stream().map(BannerJson::de).toList();
    }

    @PostMapping(value = "/api/sistema/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UUID> crear(@RequestPart(required = false) MultipartFile imagen,
                            @RequestParam(required = false) String textoAlternativo,
                            @RequestParam(required = false) String enlace,
                            @RequestParam(required = false) Boolean activo,
                            @RequestParam(required = false) LocalDate desde,
                            @RequestParam(required = false) LocalDate hasta) throws IOException {
        BannerNuevo datos = new BannerNuevo(textoAlternativo, enlace, !Boolean.FALSE.equals(activo), desde, hasta);
        return Map.of("id", vidriera.crear(datos, revisar(imagen)));
    }

    @PutMapping("/api/sistema/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable UUID id, @RequestBody DatosJson datos) {
        vidriera.editar(id, new BannerNuevo(datos.textoAlternativo(), datos.enlace(), Boolean.TRUE.equals(datos.activo()),
                datos.desde(), datos.hasta()));
    }

    @PutMapping(value = "/api/sistema/banners/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiarImagen(@PathVariable UUID id, @RequestPart(required = false) MultipartFile imagen) throws IOException {
        vidriera.cambiarImagen(id, revisar(imagen));
    }

    @DeleteMapping("/api/sistema/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrar(@PathVariable UUID id) {
        vidriera.borrar(id);
    }

    @PutMapping("/api/sistema/banners/orden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ordenar(@RequestBody OrdenJson orden) {
        vidriera.ordenar(orden.orden());
    }

    private static ArchivoSubido revisar(MultipartFile imagen) throws IOException {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("Elegí la imagen del banner");
        }
        return ArchivoSubido.revisar(imagen.getOriginalFilename(), imagen.getBytes(), IMAGENES, ArchivoSubido.TAMANO_MAXIMO_FOTO);
    }

    static String urlDeImagen(DatosBanner b) {
        return b.imagenHeredada() != null ? b.imagenHeredada() : "/api/publico/banners/" + b.id() + "/imagen?v=" + b.version();
    }

    // ─── Errores (solo de este controller) ────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del banner"));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ResponseEntity<Map<String, String>> archivoInvalido(ArchivoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "archivo_invalido", "detalle", e.getMessage()));
    }

    @ExceptionHandler(BannerNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "banner_no_encontrado"));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }

    // ─── JSON ─────────────────────────────────────────────────────────────────────────────────

    /** La forma de "Promocion" en web/src/lib/api/tipos.ts. */
    record PromocionJson(String id, String imagen, String textoAlternativo, String enlace) {
        static PromocionJson de(DatosBanner b) {
            return new PromocionJson(b.id().toString(), urlDeImagen(b), b.textoAlternativo(), b.enlace());
        }
    }

    record BannerJson(UUID id, String imagen, String textoAlternativo, String enlace, boolean activo, LocalDate desde,
                      LocalDate hasta, int orden, boolean visibleHoy) {
        static BannerJson de(DatosBanner b) {
            return new BannerJson(b.id(), urlDeImagen(b), b.textoAlternativo(), b.enlace(), b.activo(), b.desde(), b.hasta(),
                    b.orden(), b.visibleHoy());
        }
    }

    record DatosJson(String textoAlternativo, String enlace, Boolean activo, LocalDate desde, LocalDate hasta) {
    }

    record OrdenJson(List<UUID> orden) {
    }
}
