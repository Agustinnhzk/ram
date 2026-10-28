package com.ram.tenants.aplicacion;

import com.ram.tenants.dominio.Dominio;
import com.ram.tenants.dominio.Taller;

import java.util.Objects;

/**
 * Caso de uso: a partir del host de una request, encontrar a qué taller pertenece (ADR-002).
 *
 * <pre>
 * ram.plataforma.com.ar            → subdominio de la plataforma → busca por slug "ram"
 * ramservicios.com.ar              → dominio propio              → busca por dominio
 * sistema.ramservicios.com.ar      → se quita "sistema." y se resuelve igual (ADR-003)
 * </pre>
 *
 * <p>No tiene anotaciones de Spring: se crea en la configuración del módulo. Así se testea
 * con un simple {@code new}, sin levantar el framework.
 */
public class ResolverTaller {

    /** El sistema de gestión vive en "sistema." + el dominio de la tienda del taller. */
    private static final String PREFIJO_SISTEMA = "sistema.";

    private final TalleresRepositorio talleres;
    private final Dominio dominioPlataforma;

    public ResolverTaller(TalleresRepositorio talleres, Dominio dominioPlataforma) {
        this.talleres = Objects.requireNonNull(talleres);
        this.dominioPlataforma = Objects.requireNonNull(dominioPlataforma);
    }

    /**
     * @throws TallerNoEncontradoException si el host no corresponde a ningún taller
     * @throws TallerSuspendidoException   si el taller existe pero está suspendido
     */
    public Taller ejecutar(String host) {
        Dominio dominio = Dominio.intentar(host)
                .map(d -> d.sinPrefijo(PREFIJO_SISTEMA))
                .orElseThrow(() -> new TallerNoEncontradoException(host));

        // Si es un subdominio de la plataforma se busca SOLO por slug; si no, SOLO por dominio propio.
        Taller taller = dominio.subdominioDe(dominioPlataforma)
                .map(talleres::buscarPorSlug)
                .orElseGet(() -> talleres.buscarPorDominioPropio(dominio))
                .orElseThrow(() -> new TallerNoEncontradoException(host));

        if (!taller.estaActivo()) {
            throw new TallerSuspendidoException(taller.slug());
        }
        return taller;
    }
}
