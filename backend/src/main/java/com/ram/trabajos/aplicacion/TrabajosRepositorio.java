package com.ram.trabajos.aplicacion;

import com.ram.archivos.ImagenLista;
import com.ram.trabajos.dominio.Trabajo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: dónde viven los trabajos y sus fotos (en la base, como las de productos). */
public interface TrabajosRepositorio {

    /** Del más reciente al más viejo (por fecha del trabajo y, a igual fecha, por carga). */
    List<Trabajo> todos();

    Optional<Trabajo> porId(UUID id);

    Optional<Trabajo> porSlug(String slug);

    boolean existeSlug(String slug);

    void guardar(Trabajo trabajo);

    /** El trabajo con sus fotos. */
    void borrar(UUID id);

    // ─── Fotos ───────────────────────────────────────────────────────────────────────────────

    /** Las fotos de un trabajo, en orden. */
    List<UUID> fotosDe(UUID trabajo);

    /** Las de todos los trabajos del taller en una sola consulta (para los listados). */
    Map<UUID, List<UUID>> fotosDeTodos();

    /** De qué trabajo es una foto. */
    Optional<UUID> trabajoDeLaFoto(UUID foto);

    /** La agrega al final. */
    UUID agregarFoto(UUID trabajo, ImagenLista grande, ImagenLista chica);

    void borrarFoto(UUID foto);

    /** Guarda el orden (la lista tiene exactamente las fotos del trabajo). */
    void ordenarFotos(UUID trabajo, List<UUID> fotos);

    Optional<byte[]> archivo(UUID foto, boolean chica);
}
