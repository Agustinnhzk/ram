package com.ram.notificaciones;

import java.util.Objects;

/**
 * Un mail para una persona puntual (un cliente): por ejemplo, el link para cambiar la contraseña.
 * Lo manda el taller; el "De:" es el remitente configurado.
 */
public record MensajeAUnaPersona(String destinatario, String asunto, String texto) {

    public MensajeAUnaPersona {
        Objects.requireNonNull(destinatario);
        Objects.requireNonNull(asunto);
        Objects.requireNonNull(texto);
    }

    /** Sin el texto: puede llevar un link privado (por ejemplo, el de recuperar la contraseña). */
    @Override
    public String toString() {
        return "MensajeAUnaPersona[" + destinatario + ", " + asunto + "]";
    }
}
