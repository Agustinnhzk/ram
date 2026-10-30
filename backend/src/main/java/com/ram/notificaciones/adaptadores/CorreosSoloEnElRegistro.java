package com.ram.notificaciones.adaptadores;

import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAUnaPersona;
import com.ram.notificaciones.MensajeAlTaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cuando no hay servidor de correo configurado (los tests, o tu máquina sin credenciales):
 * en vez de fallar, escribe el mail en el log. Así todo funciona igual y se ve qué se hubiera mandado.
 */
class CorreosSoloEnElRegistro implements Correos {

    private static final Logger LOG = LoggerFactory.getLogger(CorreosSoloEnElRegistro.class);

    @Override
    public void enviarAlTaller(MensajeAlTaller mensaje) {
        LOG.info("[MAIL NO ENVIADO: falta spring.mail.host] Para el taller — {}\n{}\nAdjuntos: {}",
                mensaje.asunto(), mensaje.texto(), mensaje.adjuntos());
    }

    /** En desarrollo, el link de "olvidé mi contraseña" aparece acá: copialo del log para probar. */
    @Override
    public void enviarA(MensajeAUnaPersona mensaje) {
        LOG.info("[MAIL NO ENVIADO: falta spring.mail.host] Para {} — {}\n{}", mensaje.destinatario(), mensaje.asunto(), mensaje.texto());
    }
}
