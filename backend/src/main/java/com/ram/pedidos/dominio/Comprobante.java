package com.ram.pedidos.dominio;

import com.ram.archivos.TipoDeArchivo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Los datos de un comprobante subido (el archivo se guarda aparte). */
public final class Comprobante {

    private final UUID id;
    private final String nombre;
    private final TipoDeArchivo tipo;
    private final int tamano;
    private final Instant subidoEn;
    private boolean rechazado;
    private String motivoRechazo;

    public Comprobante(UUID id, String nombre, TipoDeArchivo tipo, int tamano, Instant subidoEn,
                       boolean rechazado, String motivoRechazo) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.tipo = Objects.requireNonNull(tipo);
        this.tamano = tamano;
        this.subidoEn = Objects.requireNonNull(subidoEn);
        this.rechazado = rechazado;
        this.motivoRechazo = motivoRechazo;
    }

    void rechazar(String motivo) {
        this.rechazado = true;
        this.motivoRechazo = motivo;
    }

    public UUID id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public TipoDeArchivo tipo() {
        return tipo;
    }

    public int tamano() {
        return tamano;
    }

    public Instant subidoEn() {
        return subidoEn;
    }

    public boolean rechazado() {
        return rechazado;
    }

    public String motivoRechazo() {
        return motivoRechazo;
    }
}
