package com.ram.archivos;

/** El archivo está vacío, es muy grande o no es de un tipo aceptado. El mensaje se le puede mostrar a la persona. */
public class ArchivoInvalidoException extends RuntimeException {

    public ArchivoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
