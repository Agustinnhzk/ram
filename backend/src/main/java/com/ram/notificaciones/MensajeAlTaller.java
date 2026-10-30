package com.ram.notificaciones;

import java.util.List;
import java.util.Objects;

/**
 * Un mail PARA EL TALLER (una postulación, un pedido nuevo...). A quién llega lo decide la configuración
 * (ram.correo.destino-del-taller), no quien lo manda.
 *
 * @param responderA si no es null, al tocar "Responder" el mail va a esta dirección (la del cliente)
 */
public record MensajeAlTaller(String asunto, String texto, String responderA, List<Adjunto> adjuntos) {

    public MensajeAlTaller {
        Objects.requireNonNull(asunto);
        Objects.requireNonNull(texto);
        adjuntos = adjuntos == null ? List.of() : List.copyOf(adjuntos);
    }
}
