package com.ram.pedidos;

/** Evento: el cliente subió el comprobante. El taller tiene que revisarlo. */
public record PagoInformado(int numero, String cliente, String telefono, long total) {
}
