package com.ram.usuarios.aplicacion;

import com.ram.usuarios.Rol;
import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.ClaveNueva;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;
import com.ram.usuarios.dominio.Personal;

import java.util.Objects;
import java.util.Optional;

/**
 * Casos de uso de acceso: iniciar sesión, registrarse, dar de alta personal.
 *
 * <p>No sabe nada de HTTP, cookies ni sesiones: solo decide si alguien puede entrar.
 * Cómo se guarda la sesión lo resuelve el adaptador web.
 */
public class ServicioDeAcceso {

    private final PersonalRepositorio personal;
    private final CuentasClientesRepositorio cuentas;
    private final CifradorDeClaves cifrador;

    /**
     * Si el correo no existe, igual se compara la clave contra este hash.
     * Sin esto, "correo inexistente" respondería en 1 ms y "clave incorrecta" en 100 ms (lo que tarda BCrypt),
     * y midiendo el tiempo de respuesta se podría averiguar qué correos tienen cuenta.
     */
    private final String hashParaIgualarTiempos;

    public ServicioDeAcceso(PersonalRepositorio personal, CuentasClientesRepositorio cuentas, CifradorDeClaves cifrador) {
        this.personal = Objects.requireNonNull(personal);
        this.cuentas = Objects.requireNonNull(cuentas);
        this.cifrador = Objects.requireNonNull(cifrador);
        this.hashParaIgualarTiempos = cifrador.cifrar("clave-ficticia-para-igualar-tiempos");
    }

    /** @throws CredencialesInvalidasException si el correo o la clave no son correctos, o el usuario está desactivado */
    public Acceso iniciarSesionPersonal(String correo, String clave) {
        Optional<Personal> encontrado = CorreoDeAcceso.intentar(correo).flatMap(personal::buscarPorCorreo);
        if (encontrado.isEmpty()) {
            cifrador.coincide(textoSeguro(clave), hashParaIgualarTiempos);
            throw new CredencialesInvalidasException();
        }
        Personal p = encontrado.get();
        boolean claveCorrecta = cifrador.coincide(textoSeguro(clave), p.claveCifrada());
        if (!claveCorrecta || !p.estaActivo()) {
            throw new CredencialesInvalidasException();
        }
        return Acceso.dePersonal(p);
    }

    /** @throws CredencialesInvalidasException si el correo o la clave no son correctos */
    public Acceso iniciarSesionCliente(String correo, String clave) {
        Optional<CuentaCliente> encontrada = CorreoDeAcceso.intentar(correo).flatMap(cuentas::buscarPorCorreo);
        if (encontrada.isEmpty()) {
            cifrador.coincide(textoSeguro(clave), hashParaIgualarTiempos);
            throw new CredencialesInvalidasException();
        }
        CuentaCliente cuenta = encontrada.get();
        if (!cifrador.coincide(textoSeguro(clave), cuenta.claveCifrada())) {
            throw new CredencialesInvalidasException();
        }
        return Acceso.deCliente(cuenta);
    }

    /**
     * @throws IllegalArgumentException     si algún dato no es válido (nombre, correo, clave)
     * @throws CorreoYaRegistradoException  si ya hay una cuenta con ese correo en este taller
     */
    public Acceso registrarCliente(String nombre, String correo, String clave) {
        CorreoDeAcceso correoDeAcceso = new CorreoDeAcceso(correo);
        ClaveNueva claveNueva = new ClaveNueva(clave);
        if (cuentas.existeCorreo(correoDeAcceso)) {
            throw new CorreoYaRegistradoException();
        }
        CuentaCliente cuenta = CuentaCliente.registrar(nombre, correoDeAcceso, cifrador.cifrar(claveNueva.valor()));
        cuentas.guardar(cuenta);
        return Acceso.deCliente(cuenta);
    }

    /**
     * @throws IllegalArgumentException     si algún dato no es válido
     * @throws CorreoYaRegistradoException  si ya hay personal con ese correo en este taller
     */
    public UsuarioId darDeAltaPersonal(String nombre, String correo, String clave, Rol rol) {
        CorreoDeAcceso correoDeAcceso = new CorreoDeAcceso(correo);
        ClaveNueva claveNueva = new ClaveNueva(clave);
        if (personal.existeCorreo(correoDeAcceso)) {
            throw new CorreoYaRegistradoException();
        }
        Personal nuevo = Personal.darDeAlta(nombre, correoDeAcceso, cifrador.cifrar(claveNueva.valor()), rol);
        personal.guardar(nuevo);
        return nuevo.id();
    }

    public boolean hayPersonal() {
        return personal.hayAlguno();
    }

    private static String textoSeguro(String clave) {
        return clave == null ? "" : clave;
    }
}
