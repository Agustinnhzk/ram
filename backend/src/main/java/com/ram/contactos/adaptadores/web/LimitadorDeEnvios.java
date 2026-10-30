package com.ram.contactos.adaptadores.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Frena a quien quiera llenar de mails al taller: hasta 5 envíos por IP cada hora (sumando postulaciones y
 * consultas). Una persona de verdad manda uno, quizás dos si se equivocó.
 *
 * <p>Vive en memoria, como el limitador del seguimiento (ADR-009): alcanza con un solo servidor.
 */
public class LimitadorDeEnvios {

    static final int ENVIOS_POR_IP = 5;
    static final Duration VENTANA = Duration.ofHours(1);
    private static final int LIMPIAR_CADA = 500;

    private final Clock reloj;
    private final Map<String, Deque<Instant>> enviosPorIp = new HashMap<>();
    private int operaciones;

    public LimitadorDeEnvios(Clock reloj) {
        this.reloj = reloj;
    }

    /** Registra el envío y dice si se puede hacer. */
    public synchronized boolean puedeEnviar(String ip) {
        Instant ahora = reloj.instant();
        Instant desde = ahora.minus(VENTANA);
        if (++operaciones % LIMPIAR_CADA == 0) {
            enviosPorIp.values().removeIf(marcas -> marcas.isEmpty() || !marcas.peekLast().isAfter(desde));
        }
        Deque<Instant> marcas = enviosPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());
        while (!marcas.isEmpty() && !marcas.peekFirst().isAfter(desde)) {
            marcas.pollFirst();
        }
        if (marcas.size() >= ENVIOS_POR_IP) {
            return false;
        }
        marcas.addLast(ahora);
        return true;
    }
}
