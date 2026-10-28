package com.ram.usuarios.dominio;

import com.ram.usuarios.Rol;
import com.ram.usuarios.UsuarioId;

import java.util.Objects;

/**
 * Una persona que trabaja en el taller y entra al sistema de gestión.
 *
 * <p>Guarda la contraseña ya cifrada: el dominio nunca ve ni guarda una contraseña en claro.
 */
public class Personal {

    private static final int LARGO_MAXIMO_NOMBRE = 120;

    private final UsuarioId id;
    private String nombre;
    private final CorreoDeAcceso correo;
    private String claveCifrada;
    private Rol rol;
    private boolean activo;

    private Personal(UsuarioId id, String nombre, CorreoDeAcceso correo, String claveCifrada, Rol rol, boolean activo) {
        this.id = Objects.requireNonNull(id);
        this.nombre = validarNombre(nombre);
        this.correo = Objects.requireNonNull(correo, "El correo es obligatorio");
        this.claveCifrada = Objects.requireNonNull(claveCifrada, "La contraseña es obligatoria");
        this.rol = Objects.requireNonNull(rol, "El rol es obligatorio");
        this.activo = activo;
    }

    public static Personal darDeAlta(String nombre, CorreoDeAcceso correo, String claveCifrada, Rol rol) {
        return new Personal(UsuarioId.nuevo(), nombre, correo, claveCifrada, rol, true);
    }

    public static Personal reconstruir(UsuarioId id, String nombre, CorreoDeAcceso correo,
                                       String claveCifrada, Rol rol, boolean activo) {
        return new Personal(id, nombre, correo, claveCifrada, rol, activo);
    }

    /** Un empleado que se va no se borra: se desactiva, así su historial (qué ingresos cargó) se conserva. */
    public void desactivar() {
        this.activo = false;
    }

    public void reactivar() {
        this.activo = true;
    }

    public void cambiarRol(Rol nuevoRol) {
        this.rol = Objects.requireNonNull(nuevoRol);
    }

    public void cambiarClave(String nuevaClaveCifrada) {
        this.claveCifrada = Objects.requireNonNull(nuevaClaveCifrada);
    }

    public UsuarioId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public CorreoDeAcceso correo() {
        return correo;
    }

    public String claveCifrada() {
        return claveCifrada;
    }

    public Rol rol() {
        return rol;
    }

    public boolean estaActivo() {
        return activo;
    }

    static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String limpio = nombre.trim().replaceAll("\\s+", " ");
        if (limpio.length() > LARGO_MAXIMO_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede superar " + LARGO_MAXIMO_NOMBRE + " caracteres");
        }
        return limpio;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Personal otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
