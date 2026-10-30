package com.ram.instalaciones;

import java.util.List;
import java.util.UUID;

/**
 * Un abonado de monitoreo y sus 12 meses de un año.
 *
 * @param pagos índice 0 = enero … 11 = diciembre; true = pagó
 */
public record FilaDeAbonos(UUID id, UUID clienteId, String cliente, String telefono, String direccion, Long monto,
                           boolean activo, List<Boolean> pagos) {
}
