package com.ram.trabajos;

import com.ram.archivos.ArchivoSubido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de entrada del módulo. Siempre sobre el taller actual. Lo que modifica: solo el Dueño. */
public interface Trabajos {

    // ─── Tienda (sin login) ──────────────────────────────────────────────────────────────────

    /** Los publicados, del más reciente al más viejo. */
    List<DatosTrabajo> publicados();

    Optional<DatosTrabajo> publicado(String slug);

    /** Una foto de un trabajo PUBLICADO (JPEG). La de uno sin publicar no se ve desde la tienda. */
    Optional<byte[]> fotoPublica(UUID foto, boolean chica);

    // ─── Sistema ─────────────────────────────────────────────────────────────────────────────

    /** Todos (publicados o no), del más reciente al más viejo. */
    List<DatosTrabajo> todos();

    Optional<DatosTrabajo> obtener(UUID id);

    /** Los barrios ya usados, para sugerirlos al escribir (uno por zona, como se escribió la última vez). */
    List<String> barrios();

    UUID crear(TrabajoNuevo datos);

    void editar(UUID id, TrabajoNuevo datos);

    /** Con sus fotos. */
    void borrar(UUID id);

    UUID agregarFoto(UUID trabajo, ArchivoSubido imagen);

    void borrarFoto(UUID trabajo, UUID foto);

    /** El nuevo orden: todas las fotos del trabajo, una vez cada una. */
    void ordenarFotos(UUID trabajo, List<UUID> orden);

    /** Cualquier foto (publicado o no), para el sistema. */
    Optional<byte[]> foto(UUID foto, boolean chica);
}
