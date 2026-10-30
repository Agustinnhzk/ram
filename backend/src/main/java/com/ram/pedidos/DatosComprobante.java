package com.ram.pedidos;

import java.time.Instant;
import java.util.UUID;

/** Un comprobante subido, sin el archivo. @param motivoRechazo null si no fue rechazado */
public record DatosComprobante(UUID id, String nombre, String tipoDeContenido, int tamano, Instant subidoEn,
                               boolean rechazado, String motivoRechazo) {
}
