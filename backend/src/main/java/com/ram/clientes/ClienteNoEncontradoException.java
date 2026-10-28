package com.ram.clientes;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(ClienteId id) {
        super("No existe el cliente " + id);
    }
}
