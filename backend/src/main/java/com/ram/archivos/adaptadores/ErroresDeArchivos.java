package com.ram.archivos.adaptadores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * Un archivo más grande que spring.servlet.multipart.max-file-size se corta ANTES de llegar a cualquier controller
 * (Spring todavía no sabe a cuál iba). Por eso este manejador es para toda la aplicación, no de un módulo.
 */
@RestControllerAdvice
class ErroresDeArchivos {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> demasiadoGrande() {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(Map.of("error", "archivo_invalido", "detalle", "El archivo es demasiado grande (máximo 5 MB; las fotos, 10 MB)"));
    }
}
