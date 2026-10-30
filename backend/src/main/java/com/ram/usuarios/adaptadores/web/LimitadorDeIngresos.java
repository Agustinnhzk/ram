package com.ram.usuarios.adaptadores.web;

import com.ram.usuarios.aplicacion.CredencialesInvalidasException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Frena a quien prueba contraseñas (ADR-016). Cuenta los intentos FALLIDOS en los últimos 15 minutos:
 * <ul>
 *   <li>5 por correo: protege una cuenta aunque el atacante cambie de IP.</li>
 *   <li>20 por IP: frena a quien prueba muchos correos desde el mismo lugar.</li>
 * </ul>
 * Un ingreso correcto borra los fallos de ese correo. En memoria, como los otros limitadores (ADR-009):
 * con un solo servidor alcanza; si un día hay varios, se pasa a la base o a Redis.
 */
@Component
class LimitadorDeIngresos {

    static final int FALLOS_POR_CORREO = 5;
    static final int FALLOS_POR_IP = 20;
    static final Duration VENTANA = Duration.ofMinutes(15);

    private final Clock reloj;
    private final Map<String, Deque<Instant>> fallos = new HashMap<>();
    private int operaciones;

    LimitadorDeIngresos() {
        this(Clock.systemUTC());
    }

    LimitadorDeIngresos(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Intenta el ingreso con límite: si ya hubo demasiados fallos, ni lo intenta (429); si la clave está mal,
     * anota el fallo; si entra, borra los fallos de ese correo.
     */
    <T> T proteger(String ip, String correo, Supplier<T> ingresar) {
        if (!puedeIntentar(ip, correo)) {
            throw new DemasiadosIntentosException();
        }
        try {
            T resultado = ingresar.get();
            registrarExito(correo);
            return resultado;
        } catch (CredencialesInvalidasException e) {
            registrarFallo(ip, correo);
            throw e;
        }
    }

    synchronized boolean puedeIntentar(String ip, String correo) {
        Instant desde = reloj.instant().minus(VENTANA);
        if (++operaciones % 500 == 0) {
            fallos.values().removeIf(m -> m.isEmpty() || !m.peekLast().isAfter(desde));
        }
        return vigentes(claveIp(ip), desde).size() < FALLOS_POR_IP
                && vigentes(claveCorreo(correo), desde).size() < FALLOS_POR_CORREO;
    }

    synchronized void registrarFallo(String ip, String correo) {
        Instant ahora = reloj.instant();
        fallos.computeIfAbsent(claveIp(ip), k -> new ArrayDeque<>()).addLast(ahora);
        fallos.computeIfAbsent(claveCorreo(correo), k -> new ArrayDeque<>()).addLast(ahora);
    }

    synchronized void registrarExito(String correo) {
        fallos.remove(claveCorreo(correo));
    }

    private static String claveIp(String ip) {
        return "ip:" + ip;
    }

    private static String claveCorreo(String correo) {
        return "correo:" + (correo == null ? "" : correo.strip().toLowerCase(Locale.ROOT));
    }

    private Deque<Instant> vigentes(String clave, Instant desde) {
        Deque<Instant> lista = fallos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        while (!lista.isEmpty() && !lista.peekFirst().isAfter(desde)) {
            lista.pollFirst();
        }
        return lista;
    }
}
