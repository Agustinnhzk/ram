package com.ram.usuarios.dominio;

import com.ram.usuarios.UsuarioId;

import java.util.Objects;

/**
 * La cuenta de un cliente en la tienda del taller.
 *
 * <p>No es lo mismo que la ficha del cliente (módulo clientes): la ficha la carga el taller
 * en el mostrador; la cuenta la crea el cliente para comprar. Con el primer pedido se vinculan (ADR-014),
 * y en "Mis pedidos" el cliente ve también sus reparaciones.
 */
public class CuentaCliente {

    private final UsuarioId id;
    private String nombre;
    private final CorreoDeAcceso correo;
    private String claveCifrada;
    private boolean correoVerificado;

    private CuentaCliente(UsuarioId id, String nombre, CorreoDeAcceso correo, String claveCifrada, boolean correoVerificado) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Personal.validarNombre(nombre);
        this.correo = Objects.requireNonNull(correo, "El correo es obligatorio");
        this.claveCifrada = Objects.requireNonNull(claveCifrada, "La contraseña es obligatoria");
        this.correoVerificado = correoVerificado;
    }

    /** Toda cuenta nueva empieza con el correo sin verificar. */
    public static CuentaCliente registrar(String nombre, CorreoDeAcceso correo, String claveCifrada) {
        return new CuentaCliente(UsuarioId.nuevo(), nombre, correo, claveCifrada, false);
    }

    public static CuentaCliente reconstruir(UsuarioId id, String nombre, CorreoDeAcceso correo,
                                            String claveCifrada, boolean correoVerificado) {
        return new CuentaCliente(id, nombre, correo, claveCifrada, correoVerificado);
    }

    public void marcarCorreoVerificado() {
        this.correoVerificado = true;
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

    public boolean correoVerificado() {
        return correoVerificado;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CuentaCliente otra && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
