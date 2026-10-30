package com.ram.usuarios.adaptadores.web;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Frena el abuso de "olvidé mi contraseña": que alguien le llene la casilla de mails a otro, o que use el
 * formulario para mandar mails en masa. 3 pedidos por correo y 10 por IP, cada hora. En memoria (ADR-009).
 */
@Component
class LimitadorDeRecuperaciones {

    static final int POR_CORREO = 3;
    static final int POR_IP = 10;
    static final Duration VENTANA = Duration.ofHours(1);

    private final Clock reloj;
    private final Map<String, Deque<Instant>> marcas = new HashMap<>();
    private int operaciones;

    LimitadorDeRecuperaciones() {
        this(Clock.systemUTC());
    }

    LimitadorDeRecuperaciones(Clock reloj) {
        this.reloj = reloj;
    }

    synchronized boolean puedePedir(String ip, String correo) {
        Instant ahora = reloj.instant();
        Instant desde = ahora.minus(VENTANA);
        if (++operaciones % 500 == 0) {
            marcas.values().removeIf(m -> m.isEmpty() || !m.peekLast().isAfter(desde));
        }
        Deque<Instant> deIp = vigentes("ip:" + ip, desde);
        Deque<Instant> deCorreo = vigentes("correo:" + correo, desde);
        if (deIp.size() >= POR_IP || deCorreo.size() >= POR_CORREO) {
            return false;
        }
        deIp.addLast(ahora);
        deCorreo.addLast(ahora);
        return true;
    }

    private Deque<Instant> vigentes(String clave, Instant desde) {
        Deque<Instant> lista = marcas.computeIfAbsent(clave, k -> new ArrayDeque<>());
        while (!lista.isEmpty() && !lista.peekFirst().isAfter(desde)) {
            lista.pollFirst();
        }
        return lista;
    }
}
