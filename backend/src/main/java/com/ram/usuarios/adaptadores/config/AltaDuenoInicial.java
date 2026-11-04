package com.ram.usuarios.adaptadores.config;

import com.ram.tenants.ContextoDeTaller;
import com.ram.tenants.TallerId;
import com.ram.usuarios.Rol;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * El problema del huevo y la gallina: para dar de alta usuarios hay que estar logueado,
 * y para loguearse hace falta un usuario.
 *
 * <p>Al arrancar, si está configurado {@link DuenoInicialProperties} y el taller todavía no tiene
 * personal, crea al dueño. Si ya hay alguien, no hace nada: es seguro dejarlo configurado.
 */
@Component
class AltaDuenoInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AltaDuenoInicial.class);

    private final DuenoInicialProperties propiedades;
    private final ContextoDeTaller contexto;
    private final ServicioDeAcceso acceso;

    AltaDuenoInicial(DuenoInicialProperties propiedades, ContextoDeTaller contexto, ServicioDeAcceso acceso) {
        this.propiedades = propiedades;
        this.contexto = contexto;
        this.acceso = acceso;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!propiedades.configurado()) {
            return;
        }
        TallerId taller = contexto.buscarPorSlug(propiedades.taller())
                .orElseThrow(() -> new IllegalStateException(
                        "ram.dueno-inicial.taller: no existe el taller '" + propiedades.taller() + "'"));

        contexto.ejecutarComo(taller, () -> {
            if (acceso.hayPersonal()) {
                log.info("El taller '{}' ya tiene personal: no se crea el dueño inicial", propiedades.taller());
                return;
            }
            acceso.darDeAltaPersonal(propiedades.nombre(), propiedades.correo(), propiedades.clave(), Rol.DUENO);
            log.info("Dueño inicial creado en el taller '{}' con el correo {}", propiedades.taller(), propiedades.correo());
        });
    }
}
