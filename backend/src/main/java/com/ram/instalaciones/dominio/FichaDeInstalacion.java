package com.ram.instalaciones.dominio;

import com.ram.instalaciones.EstadoInstalacion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Una instalación como se guarda. Se valida al crearse: si existe, es válida. */
public record FichaDeInstalacion(UUID id, int numero, UUID clienteId, String direccion, EstadoInstalacion estado,
                                 String equipos, Integer camaras, LocalDate fecha, String tecnico, Long presupuesto,
                                 String notas, Instant creadaEn) {

    public FichaDeInstalacion {
        Objects.requireNonNull(id);
        Objects.requireNonNull(clienteId, "Falta el cliente");
        Objects.requireNonNull(creadaEn);
        if (numero <= 0) {
            throw new IllegalArgumentException("El número tiene que ser positivo");
        }
        direccion = Textos.obligatorio(direccion, "la dirección", 300);
        estado = estado == null ? EstadoInstalacion.VISITA : estado;
        equipos = Textos.opcional(equipos, "los equipos", 1000);
        tecnico = Textos.opcional(tecnico, "el técnico", 120);
        notas = Textos.opcional(notas, "las notas", 2000);
        if (camaras != null && (camaras < 0 || camaras > 500)) {
            throw new IllegalArgumentException("La cantidad de cámaras no es válida");
        }
        if (presupuesto != null && (presupuesto <= 0 || presupuesto > 1_000_000_000L)) {
            throw new IllegalArgumentException("El presupuesto tiene que ser mayor a cero");
        }
    }
}
