package com.ram.instalaciones;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Una instalación, con los datos del cliente para mostrarla. */
public record Instalacion(UUID id, int numero, UUID clienteId, String cliente, String telefono, String direccion,
                          EstadoInstalacion estado, String equipos, Integer camaras, LocalDate fecha, String tecnico,
                          Long presupuesto, String notas, Instant creadaEn) {
}
