package com.ram.pedidos.adaptadores.web;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.pedidos.OperacionNoPermitidaException;
import com.ram.pedidos.PedidoNoEncontradoException;
import com.ram.pedidos.ProductoNoDisponibleException;
import com.ram.pedidos.SinPermisoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Errores de los controllers de pedidos. Los "detalle" se le pueden mostrar a la persona. */
@RestControllerAdvice(assignableTypes = {TiendaPedidosController.class, SistemaPedidosController.class, ArmadoDePcController.class})
class ErroresDePedidos {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del pedido"));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ResponseEntity<Map<String, String>> archivoInvalido(ArchivoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "archivo_invalido", "detalle", e.getMessage()));
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    ResponseEntity<Map<String, String>> noEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "pedido_no_encontrado"));
    }

    @ExceptionHandler(ProductoNoDisponibleException.class)
    ResponseEntity<Map<String, String>> productoNoDisponible(ProductoNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "producto_no_disponible", "detalle", e.getMessage()));
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    ResponseEntity<Map<String, String>> operacionNoPermitida(OperacionNoPermitidaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "operacion_no_permitida", "detalle", e.getMessage()));
    }

    @ExceptionHandler(SinPermisoException.class)
    ResponseEntity<Map<String, String>> sinPermiso(SinPermisoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "sin_permiso", "detalle", e.getMessage()));
    }

    /** Dos pedidos en el mismo instante pueden tomar el mismo número: la base frena al segundo (UNIQUE). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "conflicto", "detalle", "No pudimos guardar el pedido. Probá de nuevo."));
    }
}
