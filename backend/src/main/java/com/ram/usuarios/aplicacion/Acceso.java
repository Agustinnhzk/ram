package com.ram.usuarios.aplicacion;

import com.ram.usuarios.Rol;
import com.ram.usuarios.TipoDeCuenta;
import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CuentaCliente;
import com.ram.usuarios.dominio.Personal;

/**
 * Resultado de un login exitoso: quién entró. El adaptador web lo convierte en la sesión.
 *
 * @param rol null para clientes
 */
public record Acceso(UsuarioId id, String nombre, TipoDeCuenta tipo, Rol rol) {

    static Acceso dePersonal(Personal p) {
        return new Acceso(p.id(), p.nombre(), TipoDeCuenta.PERSONAL, p.rol());
    }

    static Acceso deCliente(CuentaCliente c) {
        return new Acceso(c.id(), c.nombre(), TipoDeCuenta.CLIENTE, null);
    }
}
