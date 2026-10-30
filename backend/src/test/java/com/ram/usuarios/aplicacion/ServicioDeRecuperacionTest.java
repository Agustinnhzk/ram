package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioDeRecuperacionTest {

    private final DoblesDeAcceso.CuentasEnMemoria cuentas = new DoblesDeAcceso.CuentasEnMemoria();
    private final DoblesDeAcceso.RecuperacionesEnMemoria recuperaciones = new DoblesDeAcceso.RecuperacionesEnMemoria();
    private final DoblesDeAcceso.CifradorFalso cifrador = new DoblesDeAcceso.CifradorFalso();
    private final List<String> enlaces = new ArrayList<>();
    private final List<UsuarioId> sesionesCerradas = new ArrayList<>();
    private Instant ahora = Instant.parse("2026-10-29T15:00:00Z");
    private int tokens;
    private ServicioDeRecuperacion servicio;
    private CuentaCliente ana;

    @BeforeEach
    void preparar() {
        Clock reloj = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zona) {
                return this;
            }

            @Override
            public Instant instant() {
                return ahora;
            }
        };
        servicio = new ServicioDeRecuperacion(cuentas, recuperaciones, cifrador,
                (correo, nombre, enlace) -> enlaces.add(correo + " " + enlace), sesionesCerradas::add, reloj,
                () -> "token-" + (++tokens));
        ana = CuentaCliente.registrar("Ana", new CorreoDeAcceso("ana@mail.com"), cifrador.cifrar("clave-vieja-1"));
        cuentas.guardar(ana);
    }

    @Test
    void mandaElLinkConElTokenDespuesDelNumeral_yGuardaSoloSuHash() {
        servicio.solicitar(" ANA@mail.com ", "http://ram.localhost:3000");

        assertThat(enlaces).containsExactly("ana@mail.com http://ram.localhost:3000/recuperar-clave/nueva#token-1");
        assertThat(recuperaciones.datos.values()).singleElement().satisfies(r -> {
            assertThat(r.tokenHash()).isEqualTo(ServicioDeRecuperacion.hash("token-1"));
            assertThat(r.tokenHash()).doesNotContain("token-1");
        });
    }

    @Test
    void siNoHayCuentaNoPasaNada_niFalla() {
        servicio.solicitar("nadie@mail.com", "http://ram.localhost:3000");
        servicio.solicitar("esto no es un correo", "http://ram.localhost:3000");

        assertThat(enlaces).isEmpty();
        assertThat(recuperaciones.datos.values()).isEmpty();
    }

    @Test
    void conElLinkSeEligeUnaClaveNueva_ySeCierranLasSesiones() {
        servicio.solicitar("ana@mail.com", "http://x");
        servicio.restablecer("token-1", "clave-nueva-123");

        assertThat(cuentas.buscarPorId(ana.id()).orElseThrow().claveCifrada()).isEqualTo("cifrada:clave-nueva-123");
        assertThat(sesionesCerradas).containsExactly(ana.id());
    }

    @Test
    void elLinkSirveUnaSolaVez() {
        servicio.solicitar("ana@mail.com", "http://x");
        servicio.restablecer("token-1", "clave-nueva-123");

        assertThatThrownBy(() -> servicio.restablecer("token-1", "otra-clave-456"))
                .isInstanceOf(EnlaceDeRecuperacionInvalidoException.class);
    }

    @Test
    void elLinkVenceEnUnaHora() {
        servicio.solicitar("ana@mail.com", "http://x");
        ahora = ahora.plus(Duration.ofMinutes(61));

        assertThatThrownBy(() -> servicio.restablecer("token-1", "clave-nueva-123"))
                .isInstanceOf(EnlaceDeRecuperacionInvalidoException.class);
    }

    @Test
    void pedirOtroLinkAnulaElAnterior() {
        servicio.solicitar("ana@mail.com", "http://x");
        servicio.solicitar("ana@mail.com", "http://x");

        assertThatThrownBy(() -> servicio.restablecer("token-1", "clave-nueva-123"))
                .isInstanceOf(EnlaceDeRecuperacionInvalidoException.class);
        servicio.restablecer("token-2", "clave-nueva-123");
    }

    @Test
    void unTokenInventadoOVacioNoSirve_yUnaClaveCortaSeRechazaAntes() {
        servicio.solicitar("ana@mail.com", "http://x");

        assertThatThrownBy(() -> servicio.restablecer("inventado", "clave-nueva-123"))
                .isInstanceOf(EnlaceDeRecuperacionInvalidoException.class);
        assertThatThrownBy(() -> servicio.restablecer(null, "clave-nueva-123"))
                .isInstanceOf(EnlaceDeRecuperacionInvalidoException.class);
        assertThatThrownBy(() -> servicio.restablecer("token-1", "corta"))
                .isInstanceOf(IllegalArgumentException.class);
        // La clave corta no gastó el link:
        servicio.restablecer("token-1", "clave-nueva-123");
    }
}
