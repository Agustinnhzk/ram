package com.ram.contactos;

/** Por qué escribe una empresa. */
public enum MotivoDeEmpresa {
    /** Quiere que el taller se encargue del soporte IT de la empresa. */
    TERCERIZAR_IT("Tercerizar el servicio de IT"),
    ALIANZA_COMERCIAL("Alianza comercial"),
    OTRO("Otro");

    private final String descripcion;

    MotivoDeEmpresa(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
