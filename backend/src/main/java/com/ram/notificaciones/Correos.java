package com.ram.notificaciones;

/** Puerto para mandar mails. */
public interface Correos {

    /** @throws CorreoNoEnviadoException si no se pudo mandar */
    void enviarAlTaller(MensajeAlTaller mensaje);

    /** @throws CorreoNoEnviadoException si no se pudo mandar */
    void enviarA(MensajeAUnaPersona mensaje);
}
