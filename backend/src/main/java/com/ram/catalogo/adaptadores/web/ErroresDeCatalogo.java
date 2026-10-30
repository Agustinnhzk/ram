package com.ram.catalogo.adaptadores.web;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.catalogo.ProductoNoEncontradoException;
import com.ram.catalogo.SinPermisoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Cómo se responden los errores del catálogo. assignableTypes: aplica SOLO a los controllers
 * de este módulo, así no cambia cómo responden los demás.
 */
@RestControllerAdvice(assignableTypes = {CatalogoPublicoController.class, PreciosController.class, ProductosController.class})
class ErroresDeCatalogo {

    /** Datos inválidos (los rechaza el dominio: precio negativo, porcentaje fuera de rango) → 400 con el motivo. */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ResponseEntity<Map<String, String>> archivoInvalido(ArchivoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "archivo_invalido", "detalle", e.getMessage()));
    }

    /** Un JSON que no se entiende (un tipo de componente que no existe, una fecha mal escrita). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del formulario"));
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "producto_no_encontrado"));
    }

    /** Está logueado pero no le corresponde (un técnico cambiando precios) → 403, no 401. */
    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }
}
