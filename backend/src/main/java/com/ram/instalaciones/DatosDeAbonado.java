package com.ram.instalaciones;

import java.util.UUID;

/** @param monto el abono mensual en pesos, o null si no está definido */
public record DatosDeAbonado(UUID clienteId, DatosDeInstalacion.ClienteNuevo clienteNuevo, String direccion, Long monto,
                             boolean activo) {
}
