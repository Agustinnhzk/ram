package com.ram.usuarios.aplicacion;

import com.ram.usuarios.dominio.ClaveNueva;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;
import com.ram.usuarios.dominio.Recuperacion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * "Olvidé mi contraseña" de la tienda (ADR-012).
 *
 * <ol>
 *   <li>La persona escribe su correo. Si hay una cuenta, se le manda un link con un token al azar.
 *       Si no hay, no pasa nada, pero la respuesta es la MISMA: así nadie averigua qué correos tienen cuenta.</li>
 *   <li>El link vale 1 hora y una sola vez. Pedir otro anula el anterior.</li>
 *   <li>Al elegir la contraseña nueva se cierran todas las sesiones abiertas de esa cuenta.</li>
 * </ol>
 *
 * <p>Se guarda el SHA-256 del token, no el token: quien lea la base no puede usar los links.
 * (SHA-256 alcanza acá, sin BCrypt: el token tiene 256 bits al azar, no se puede adivinar probando.)
 */
public class ServicioDeRecuperacion {

    static final Duration VIGENCIA = Duration.ofHours(1);
    private static final SecureRandom AZAR = new SecureRandom();

    private final CuentasClientesRepositorio cuentas;
    private final RecuperacionesRepositorio recuperaciones;
    private final CifradorDeClaves cifrador;
    private final AvisosDeCuenta avisos;
    private final SesionesDeUsuario sesiones;
    private final Clock reloj;
    private final Supplier<String> generadorDeTokens;

    public ServicioDeRecuperacion(CuentasClientesRepositorio cuentas, RecuperacionesRepositorio recuperaciones,
                                  CifradorDeClaves cifrador, AvisosDeCuenta avisos, SesionesDeUsuario sesiones,
                                  Clock reloj) {
        this(cuentas, recuperaciones, cifrador, avisos, sesiones, reloj, ServicioDeRecuperacion::tokenAlAzar);
    }

    ServicioDeRecuperacion(CuentasClientesRepositorio cuentas, RecuperacionesRepositorio recuperaciones,
                           CifradorDeClaves cifrador, AvisosDeCuenta avisos, SesionesDeUsuario sesiones,
                           Clock reloj, Supplier<String> generadorDeTokens) {
        this.cuentas = Objects.requireNonNull(cuentas);
        this.recuperaciones = Objects.requireNonNull(recuperaciones);
        this.cifrador = Objects.requireNonNull(cifrador);
        this.avisos = Objects.requireNonNull(avisos);
        this.sesiones = Objects.requireNonNull(sesiones);
        this.reloj = Objects.requireNonNull(reloj);
        this.generadorDeTokens = Objects.requireNonNull(generadorDeTokens);
    }

    /**
     * @param direccionDeLaTienda "https://ramservicios.com.ar" (de dónde vino el pedido): el link apunta ahí
     */
    public void solicitar(String correo, String direccionDeLaTienda) {
        Optional<CuentaCliente> cuenta = CorreoDeAcceso.intentar(correo).flatMap(cuentas::buscarPorCorreo);
        if (cuenta.isEmpty()) {
            return;
        }
        Instant ahora = reloj.instant();
        for (Recuperacion anterior : recuperaciones.sinUsarDe(cuenta.get().id())) {
            anterior.anular(ahora);
            recuperaciones.guardar(anterior);
        }
        String token = generadorDeTokens.get();
        recuperaciones.guardar(Recuperacion.nueva(cuenta.get().id(), hash(token), ahora, VIGENCIA));

        // El token va después de "#": esa parte de la dirección el navegador NO la manda al servidor,
        // así que no queda en logs ni en el encabezado Referer si la página carga algo de otro sitio.
        avisos.enviarEnlaceDeRecuperacion(cuenta.get().correo().valor(), cuenta.get().nombre(),
                direccionDeLaTienda + "/recuperar-clave/nueva#" + token);
    }

    /**
     * @throws IllegalArgumentException                si la contraseña nueva no cumple las reglas
     * @throws EnlaceDeRecuperacionInvalidoException   si el link no existe, venció o ya se usó
     */
    public void restablecer(String token, String clave) {
        ClaveNueva nueva = new ClaveNueva(clave);
        Instant ahora = reloj.instant();
        Recuperacion recuperacion = Optional.ofNullable(token).filter(t -> !t.isBlank())
                .flatMap(t -> recuperaciones.porHash(hash(t)))
                .filter(r -> r.vigente(ahora))
                .orElseThrow(EnlaceDeRecuperacionInvalidoException::new);
        CuentaCliente cuenta = cuentas.buscarPorId(recuperacion.cuenta())
                .orElseThrow(EnlaceDeRecuperacionInvalidoException::new);

        cuenta.cambiarClave(cifrador.cifrar(nueva.valor()));
        cuentas.guardar(cuenta);
        recuperacion.anular(ahora);
        recuperaciones.guardar(recuperacion);
        sesiones.cerrarTodas(cuenta.id());
    }

    static String hash(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Java siempre trae SHA-256", e);
        }
    }

    /** 32 bytes al azar (256 bits) en Base64 para URLs: 43 caracteres sin "+", "/" ni "=". */
    private static String tokenAlAzar() {
        byte[] bytes = new byte[32];
        AZAR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
