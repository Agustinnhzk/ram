package com.ram.instalaciones;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Lo que se carga o edita de una instalación.
 *
 * @param clienteId    una ficha existente; o null y entonces clienteNuevo
 * @param clienteNuevo nombre y teléfono para crear la ficha (solo al crear)
 * @param equipos      qué se instala: "DVR 8 canales, 4 cámaras Hikvision, disco 1 TB"
 */
public record DatosDeInstalacion(UUID clienteId, ClienteNuevo clienteNuevo, String direccion, EstadoInstalacion estado,
                                 String equipos, Integer camaras, LocalDate fecha, String tecnico, Long presupuesto,
                                 String notas) {

    public record ClienteNuevo(String nombreCompleto, String telefono) {
    }
}
