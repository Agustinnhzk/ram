package com.ram.reparaciones.adaptadores.web;

import com.ram.reparaciones.NumeroDeOrdenRepetidoException;
import com.ram.reparaciones.OrdenNoEncontradaException;
import com.ram.reparaciones.TransicionInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Errores de los controllers de reparaciones (solo de este módulo). */
@RestControllerAdvice(assignableTypes = {ReparacionesController.class, SeguimientoController.class})
class ErroresDeReparaciones {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(OrdenNoEncontradaException.class)
    ResponseEntity<Map<String, String>> noEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "orden_no_encontrada"));
    }

    @ExceptionHandler(NumeroDeOrdenRepetidoException.class)
    ResponseEntity<Map<String, String>> numeroRepetido(NumeroDeOrdenRepetidoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "numero_repetido", "detalle", e.getMessage()));
    }

    /**
     * Dos ingresos con el mismo número al mismo tiempo: los dos pasan el control del servicio,
     * pero la base frena al segundo (UNIQUE). Se responde igual que el número repetido.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflictoEnLaBase(DataIntegrityViolationException e) {
        boolean numero = String.valueOf(e.getMessage()).contains("uq_ordenes_taller_numero");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(numero
                ? Map.of("error", "numero_repetido", "detalle", "Ya existe una orden con ese número")
                : Map.of("error", "conflicto", "detalle", "Los datos chocan con otros ya guardados"));
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    ResponseEntity<Map<String, String>> transicionInvalida(TransicionInvalidaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "transicion_invalida", "detalle", e.getMessage()));
    }
}
