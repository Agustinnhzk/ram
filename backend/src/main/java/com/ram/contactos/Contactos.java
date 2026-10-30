package com.ram.contactos;

/** Puerto de entrada del módulo. */
public interface Contactos {

    /**
     * Manda la postulación (con el CV adjunto) al mail del taller.
     *
     * @throws IllegalArgumentException si falta un dato o es inválido (el mensaje se le puede mostrar a la persona)
     * @throws com.ram.notificaciones.CorreoNoEnviadoException si el mail no salió
     */
    void postularse(Postulacion postulacion);

    /** Igual que {@link #postularse}, para una empresa. */
    void consultarComoEmpresa(ConsultaDeEmpresa consulta);
}
