package com.ram.reparaciones.adaptadores.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Frena a quien intenta adivinar los 4 dígitos del seguimiento (ADR-009). Dos topes, por ventana de 15 minutos:
 * <ul>
 *   <li><b>Por orden:</b> 5 intentos fallidos y esa orden queda bloqueada. Aunque el atacante cambie de IP,
 *       probar 10.000 combinaciones de a 5 cada 15 minutos lleva más de 20 días.</li>
 *   <li><b>Por IP:</b> 30 intentos (buenos o malos), para que nadie barra muchas órdenes distintas.</li>
 * </ul>
 *
 * <p>Vive en memoria: alcanza con un solo servidor. Con varios, se pasa a la base o a Redis (está en el ADR).
 * Es synchronized porque lo usan varias requests a la vez y el tráfico es bajo: simple y correcto.
 */
public class LimitadorDeIntentos {

    static final int FALLOS_POR_ORDEN = 5;
    static final int INTENTOS_POR_IP = 30;
    static final Duration VENTANA = Duration.ofMinutes(15);
    /** Para que la memoria no crezca sin límite: cada tanto se borran las entradas vencidas. */
    private static final int LIMPIAR_CADA = 1_000;

    private final Clock reloj;
    private final Map<String, Deque<Instant>> intentosPorIp = new HashMap<>();
    private final Map<String, Deque<Instant>> fallosPorOrden = new HashMap<>();
    private int operaciones;

    public LimitadorDeIntentos(Clock reloj) {
        this.reloj = reloj;
    }

    /** Registra el intento y dice si se puede hacer. Llamar ANTES de buscar la orden. */
    public synchronized boolean puedeIntentar(String ip, String orden) {
        Instant ahora = reloj.instant();
        limpiarCadaTanto(ahora);
        if (vigentes(fallosPorOrden, orden, ahora) >= FALLOS_POR_ORDEN) {
            return false;
        }
        if (vigentes(intentosPorIp, ip, ahora) >= INTENTOS_POR_IP) {
            return false;
        }
        intentosPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>()).addLast(ahora);
        return true;
    }

    public synchronized void registrarFallo(String orden) {
        fallosPorOrden.computeIfAbsent(orden, k -> new ArrayDeque<>()).addLast(reloj.instant());
    }

    /** El dueño de la orden acertó: se le borran los fallos (por ejemplo, si antes se equivocó un dígito). */
    public synchronized void registrarExito(String orden) {
        fallosPorOrden.remove(orden);
    }

    /** Cuántas marcas siguen dentro de la ventana (y borra las viejas). */
    private static int vigentes(Map<String, Deque<Instant>> mapa, String clave, Instant ahora) {
        Deque<Instant> marcas = mapa.get(clave);
        if (marcas == null) {
            return 0;
        }
        Instant desde = ahora.minus(VENTANA);
        while (!marcas.isEmpty() && !marcas.peekFirst().isAfter(desde)) {
            marcas.pollFirst();
        }
        return marcas.size();
    }

    private void limpiarCadaTanto(Instant ahora) {
        if (++operaciones % LIMPIAR_CADA != 0) {
            return;
        }
        Instant desde = ahora.minus(VENTANA);
        for (Map<String, Deque<Instant>> mapa : java.util.List.of(intentosPorIp, fallosPorOrden)) {
            mapa.values().removeIf(marcas -> marcas.isEmpty() || !marcas.peekLast().isAfter(desde));
        }
    }
}
