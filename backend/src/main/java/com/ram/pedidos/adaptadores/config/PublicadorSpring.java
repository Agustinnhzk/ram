package com.ram.pedidos.adaptadores.config;

import com.ram.pedidos.aplicacion.PublicadorDeEventos;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Publica con el mecanismo de eventos de Spring. Spring Modulith anota cada evento en la tabla
 * event_publication EN LA MISMA TRANSACCIÓN que el pedido (V4): si la aplicación se cae antes de mandar
 * el mail, el evento queda pendiente y no se pierde.
 */
class PublicadorSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher publicador;

    PublicadorSpring(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(Object evento) {
        publicador.publishEvent(evento);
    }
}
