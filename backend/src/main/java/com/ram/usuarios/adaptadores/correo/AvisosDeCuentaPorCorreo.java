package com.ram.usuarios.adaptadores.correo;

import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAUnaPersona;
import com.ram.usuarios.aplicacion.AvisosDeCuenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Manda el mail "de fondo" (en otro hilo, con el ejecutor de tareas que trae Spring Boot), por dos motivos:
 * <ul>
 *   <li>La persona no espera los segundos que tarda el servidor de correo.</li>
 *   <li>La respuesta tarda lo mismo haya o no una cuenta con ese correo: no se puede averiguar midiendo el tiempo.</li>
 * </ul>
 * No usa un evento con registro (como los avisos de pedidos) a propósito: el registro guarda el evento en la base,
 * y este lleva el link privado.
 */
@Component
class AvisosDeCuentaPorCorreo implements AvisosDeCuenta {

    private static final Logger LOG = LoggerFactory.getLogger(AvisosDeCuentaPorCorreo.class);

    private final Correos correos;
    private final TaskExecutor ejecutor;

    /** El nombre del parámetro elige el ejecutor de Spring Boot ("applicationTaskExecutor") si hubiera más de uno. */
    AvisosDeCuentaPorCorreo(Correos correos, TaskExecutor applicationTaskExecutor) {
        this.correos = correos;
        this.ejecutor = applicationTaskExecutor;
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String correo, String nombre, String enlace) {
        String texto = """
                Hola %s:

                Pediste cambiar la contraseña de tu cuenta. Entrá a este link para elegir una nueva:

                %s

                El link vale por 1 hora y se puede usar una sola vez.
                Si no fuiste vos, ignorá este mail: tu contraseña sigue siendo la misma.
                """.formatted(nombre, enlace);
        ejecutor.execute(() -> {
            try {
                correos.enviarA(new MensajeAUnaPersona(correo, "Cambiá tu contraseña", texto));
            } catch (RuntimeException e) {
                LOG.error("No salió el mail para cambiar la contraseña", e);
            }
        });
    }
}
