package com.ram.tenants.aplicacion;

import com.ram.tenants.dominio.Slug;

public class TallerSuspendidoException extends RuntimeException {

    public TallerSuspendidoException(Slug slug) {
        super("El taller '" + slug.valor() + "' está suspendido");
    }
}
