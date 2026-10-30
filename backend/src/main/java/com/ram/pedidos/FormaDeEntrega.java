package com.ram.pedidos;

public enum FormaDeEntrega {
    /** Lo busca en el local. */
    RETIRO,
    /** Se lo llevamos dentro de Rosario: gratis. */
    ENVIO_ROSARIO,
    /** Fuera de Rosario: el envío se cotiza por WhatsApp y se paga aparte (no está en el total). */
    ENVIO_OTRA_LOCALIDAD;

    public boolean esEnvio() {
        return this != RETIRO;
    }
}
