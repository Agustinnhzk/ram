package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.aplicacion.CorreoYaRegistradoException;
import com.ram.usuarios.aplicacion.CredencialesInvalidasException;
import com.ram.usuarios.aplicacion.EnlaceDeRecuperacionInvalidoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Traduce los errores de los casos de uso a respuestas HTTP, solo para los controladores de este paquete. */
@RestControllerAdvice(basePackageClasses = ErroresDeAcceso.class)
class ErroresDeAcceso {

    @ExceptionHandler(CredencialesInvalidasException.class)
    ResponseEntity<Map<String, String>> credencialesInvalidas() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "credenciales_invalidas"));
    }

    /** La segunda excepción cubre el caso raro de dos registros simultáneos con el mismo correo: lo frena la base. */
    @ExceptionHandler({CorreoYaRegistradoException.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String, String>> correoYaRegistrado() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "correo_ya_registrado"));
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    ResponseEntity<Map<String, String>> demasiadosIntentos(DemasiadosIntentosException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "demasiados_intentos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(EnlaceDeRecuperacionInvalidoException.class)
    ResponseEntity<Map<String, String>> enlaceInvalido(EnlaceDeRecuperacionInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "enlace_invalido", "detalle", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }
}
