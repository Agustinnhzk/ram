package com.ram.usuarios;

import com.ram.tenants.TallerId;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Quién está logueado en esta request. Es lo que se guarda en la sesión.
 *
 * <p>Detalles que no son casualidad:
 * <ul>
 *   <li><b>Serializable</b>: la sesión vive en PostgreSQL, y para guardarla Java la convierte a bytes.</li>
 *   <li><b>AuthenticatedPrincipal</b>: {@link #getName()} devuelve solo el id. Spring Session lo guarda
 *       en una columna de 100 caracteres, y sirve para cerrar todas las sesiones de un usuario.</li>
 *   <li>Guarda el <b>taller</b>: si la sesión se usa en otro taller, se rechaza (ADR-002).</li>
 * </ul>
 *
 * @param rol null para clientes de la tienda
 */
public record UsuarioAutenticado(UUID id, UUID tallerId, String nombre, TipoDeCuenta tipo, Rol rol)
        implements AuthenticatedPrincipal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public UsuarioAutenticado {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tallerId);
        Objects.requireNonNull(tipo);
        if (tipo == TipoDeCuenta.PERSONAL && rol == null) {
            throw new IllegalArgumentException("El personal siempre tiene un rol");
        }
    }

    public UsuarioId usuarioId() {
        return UsuarioId.de(id);
    }

    public TallerId taller() {
        return TallerId.de(tallerId);
    }

    public boolean esPersonal() {
        return tipo == TipoDeCuenta.PERSONAL;
    }

    /** Por ejemplo, para ver el password de un equipo: {@code tieneAlgunRol(DUENO, TECNICO)}. */
    public boolean tieneAlgunRol(Rol... roles) {
        return rol != null && Arrays.asList(roles).contains(rol);
    }

    @Override
    public String getName() {
        return id.toString();
    }
}
