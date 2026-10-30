package com.ram.usuarios.adaptadores.web;

/** Se superó el límite de intentos de ingreso: responde 429. */
class DemasiadosIntentosException extends RuntimeException {

    DemasiadosIntentosException() {
        super("Hiciste muchos intentos. Esperá 15 minutos y probá de nuevo.");
    }
}
