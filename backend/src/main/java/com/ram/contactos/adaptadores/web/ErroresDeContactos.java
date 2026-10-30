package com.ram.contactos.adaptadores.web;

import com.ram.archivos.ArchivoInvalidoException;
import com.ram.notificaciones.CorreoNoEnviadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = ContactosController.class)
class ErroresDeContactos {

    private static final Logger LOG = LoggerFactory.getLogger(ErroresDeContactos.class);

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", e.getMessage()));
    }

    /** Por ejemplo, un "motivo" que no existe. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest().body(Map.of("error", "datos_invalidos", "detalle", "Revisá los datos del formulario"));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ResponseEntity<Map<String, String>> archivoInvalido(ArchivoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "archivo_invalido", "detalle", e.getMessage()));
    }

    @ExceptionHandler(CorreoNoEnviadoException.class)
    ResponseEntity<Map<String, String>> correoNoEnviado(CorreoNoEnviadoException e) {
        // El detalle técnico (servidor caído, clave vencida) va al log para vos, no a la persona.
        LOG.error("No salió un mail de contacto", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "correo_no_enviado",
                "detalle", "No pudimos enviar tu mensaje. Probá de nuevo en unos minutos o escribinos por WhatsApp."));
    }
}
