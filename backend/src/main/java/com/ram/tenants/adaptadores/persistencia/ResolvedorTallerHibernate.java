package com.ram.tenants.adaptadores.persistencia;

import com.ram.tenants.TallerId;
import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import java.util.UUID;

/**
 * Le dice a Hibernate cuál es el taller actual. Es la pieza que hace funcionar
 * el filtro automático por taller (ADR-005).
 *
 * <p>Toda entidad que tenga un campo con {@code @TenantId}:
 * <ul>
 *   <li>al guardarse, recibe este taller automáticamente;</li>
 *   <li>al consultarse, solo devuelve filas de este taller.</li>
 * </ul>
 *
 * <p>Si no hay taller en el contexto, devuelve un id que no corresponde a ningún taller.
 * Así, un código que se olvidó del contexto no ve nada, en vez de ver todo.
 * Fallar cerrado es más seguro que fallar abierto.
 *
 * <p>Se registra en application.yml (hibernate.tenant_identifier_resolver);
 * Hibernate lo crea por su cuenta, por eso no es un bean de Spring.
 */
public class ResolvedorTallerHibernate implements CurrentTenantIdentifierResolver<UUID> {

    /** No existe ningún taller con este id: las consultas sin contexto devuelven vacío. */
    static final UUID SIN_TALLER = new UUID(0L, 0L);

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        return ContextoTaller.actual().map(TallerId::valor).orElse(SIN_TALLER);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
