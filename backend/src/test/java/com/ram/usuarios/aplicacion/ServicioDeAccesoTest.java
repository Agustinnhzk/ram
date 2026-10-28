package com.ram.usuarios.aplicacion;

import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioDeAccesoTest {

    private DoblesDeAcceso.PersonalEnMemoria personal;
    private ServicioDeAcceso acceso;

    @BeforeEach
    void preparar() {
        personal = new DoblesDeAcceso.PersonalEnMemoria();
        acceso = new ServicioDeAcceso(personal, new DoblesDeAcceso.CuentasEnMemoria(), new DoblesDeAcceso.CifradorFalso());
    }

    @Test
    void elPersonalIniciaSesionConSuCorreoYClave() {
        acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);

        Acceso resultado = acceso.iniciarSesionPersonal("AGUS@ram.com", "una-clave-segura");

        assertThat(resultado.nombre()).isEqualTo("Agustín");
        assertThat(resultado.tipo()).isEqualTo(TipoDeCuenta.PERSONAL);
        assertThat(resultado.rol()).isEqualTo(Rol.DUENO);
    }

    @Test
    void laClaveSeGuardaCifrada() {
        UsuarioId id = acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);

        assertThat(personal.datos.get(id).claveCifrada()).isNotEqualTo("una-clave-segura");
    }

    @Test
    void claveIncorrectaYCorreoInexistenteDanElMismoError() {
        acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);

        assertThatThrownBy(() -> acceso.iniciarSesionPersonal("agus@ram.com", "otra-clave"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThatThrownBy(() -> acceso.iniciarSesionPersonal("nadie@ram.com", "una-clave-segura"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThatThrownBy(() -> acceso.iniciarSesionPersonal("cualquier cosa", null))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void unEmpleadoDesactivadoNoPuedeEntrar() {
        UsuarioId id = acceso.darDeAltaPersonal("Empleado", "empleado@ram.com", "una-clave-segura", Rol.MOSTRADOR);
        personal.datos.get(id).desactivar();

        assertThatThrownBy(() -> acceso.iniciarSesionPersonal("empleado@ram.com", "una-clave-segura"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void noSePuedenRepetirCorreos() {
        acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);
        acceso.registrarCliente("Ana", "ana@mail.com", "clave-de-ana");

        assertThatThrownBy(() -> acceso.darDeAltaPersonal("Otro", "Agus@Ram.com", "una-clave-segura", Rol.TECNICO))
                .isInstanceOf(CorreoYaRegistradoException.class);
        assertThatThrownBy(() -> acceso.registrarCliente("Ana 2", "ana@mail.com", "clave-de-ana"))
                .isInstanceOf(CorreoYaRegistradoException.class);
    }

    @Test
    void elClienteSeRegistraYDespuesIniciaSesion() {
        Acceso registro = acceso.registrarCliente("Ana López", "ana@mail.com", "clave-de-ana");
        Acceso login = acceso.iniciarSesionCliente("ana@mail.com", "clave-de-ana");

        assertThat(login.id()).isEqualTo(registro.id());
        assertThat(login.tipo()).isEqualTo(TipoDeCuenta.CLIENTE);
        assertThat(login.rol()).isNull();
    }

    @Test
    void unClienteNoPuedeEntrarComoPersonalNiAlReves() {
        acceso.registrarCliente("Ana", "ana@mail.com", "clave-de-ana");
        acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);

        assertThatThrownBy(() -> acceso.iniciarSesionPersonal("ana@mail.com", "clave-de-ana"))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThatThrownBy(() -> acceso.iniciarSesionCliente("agus@ram.com", "una-clave-segura"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void sabeSiElTallerYaTienePersonal() {
        assertThat(acceso.hayPersonal()).isFalse();
        acceso.darDeAltaPersonal("Agustín", "agus@ram.com", "una-clave-segura", Rol.DUENO);
        assertThat(acceso.hayPersonal()).isTrue();
    }
}
