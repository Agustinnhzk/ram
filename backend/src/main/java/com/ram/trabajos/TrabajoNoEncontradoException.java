package com.ram.trabajos;

public class TrabajoNoEncontradoException extends RuntimeException {

    public TrabajoNoEncontradoException(String que) {
        super("No existe el trabajo " + que);
    }
}
