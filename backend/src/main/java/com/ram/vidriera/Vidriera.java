package com.ram.vidriera;

import com.ram.archivos.ArchivoSubido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de entrada del módulo. Siempre sobre el taller actual. Lo que modifica: solo el Dueño. */
public interface Vidriera {

    /** Los que se ven hoy en la tienda, en orden. */
    List<DatosBanner> paraLaTienda();

    /** Todos (activos o no, vencidos o no), para el sistema. */
    List<DatosBanner> todos();

    UUID crear(BannerNuevo datos, ArchivoSubido imagen);

    void editar(UUID id, BannerNuevo datos);

    void cambiarImagen(UUID id, ArchivoSubido imagen);

    void borrar(UUID id);

    /** El nuevo orden: todos los banners, una vez cada uno. */
    void ordenar(List<UUID> orden);

    /** La imagen subida (JPEG), para la tienda. */
    Optional<byte[]> imagen(UUID id);
}
