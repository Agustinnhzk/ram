package com.ram.contactos;

import com.ram.archivos.ArchivoSubido;

/**
 * Lo que manda una persona que quiere trabajar en el taller. Viene tal cual del formulario:
 * lo valida el servicio.
 *
 * @param area en qué le gustaría trabajar (texto libre: "Reparación de notebooks", "Instalaciones"...)
 * @param cv   ya revisado (PDF o Word, hasta 5 MB)
 */
public record Postulacion(String nombre, String correo, String telefono, String area, String mensaje, ArchivoSubido cv) {

    @Override
    public String toString() {
        return "Postulacion[" + nombre + ", " + cv + "]";
    }
}
