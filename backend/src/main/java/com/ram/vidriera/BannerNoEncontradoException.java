package com.ram.vidriera;

import java.util.UUID;

public class BannerNoEncontradoException extends RuntimeException {

    public BannerNoEncontradoException(UUID id) {
        super("No existe el banner " + id);
    }
}
