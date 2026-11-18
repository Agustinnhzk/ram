package com.ram.trabajos.adaptadores.persistencia;

import com.ram.archivos.ImagenLista;
import com.ram.trabajos.aplicacion.TrabajosRepositorio;
import com.ram.trabajos.dominio.Barrio;
import com.ram.trabajos.dominio.Trabajo;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
class TrabajosRepositorioJpa implements TrabajosRepositorio {

    private final TrabajoJpaRepository trabajos;
    private final FotoTrabajoJpaRepository fotos;
    private final ArchivoFotoTrabajoJpaRepository archivos;
    private final Clock reloj = Clock.systemUTC();

    TrabajosRepositorioJpa(TrabajoJpaRepository trabajos, FotoTrabajoJpaRepository fotos, ArchivoFotoTrabajoJpaRepository archivos) {
        this.trabajos = trabajos;
        this.fotos = fotos;
        this.archivos = archivos;
    }

    @Override
    public List<Trabajo> todos() {
        return trabajos.todos().stream().map(TrabajosRepositorioJpa::aDominio).toList();
    }

    @Override
    public Optional<Trabajo> porId(UUID id) {
        return trabajos.buscarPorId(id).map(TrabajosRepositorioJpa::aDominio);
    }

    @Override
    public Optional<Trabajo> porSlug(String slug) {
        return trabajos.buscarPorSlug(slug).map(TrabajosRepositorioJpa::aDominio);
    }

    @Override
    public boolean existeSlug(String slug) {
        return trabajos.existeSlug(slug);
    }

    @Override
    public void guardar(Trabajo t) {
        TrabajoJpaEntity entidad = trabajos.buscarPorId(t.id()).orElseGet(() -> new TrabajoJpaEntity(t.id(), t.slug(), t.creadoEn()));
        entidad.actualizar(t.tipo(), t.titulo(), t.barrio().texto(), t.barrio().clave(), t.aDomicilio(), t.problema(),
                t.diagnostico(), t.solucion(), t.video(), t.fecha(), t.numeroDeOrden(), t.consentimiento(), t.publicado());
        trabajos.save(entidad);
    }

    /** Primero los archivos, después las fotos (que los archivos referencian) y al final el trabajo. */
    @Override
    public void borrar(UUID id) {
        for (FotoTrabajoJpaEntity f : fotos.deTrabajo(id)) {
            archivos.buscarPorId(f.getId()).ifPresent(archivos::delete);
        }
        archivos.flush();
        fotos.deleteAll(fotos.deTrabajo(id));
        fotos.flush();
        trabajos.buscarPorId(id).ifPresent(trabajos::delete);
        trabajos.flush();
    }

    // ─── Fotos ───────────────────────────────────────────────────────────────────────────────

    @Override
    public List<UUID> fotosDe(UUID trabajo) {
        return fotos.deTrabajo(trabajo).stream().map(FotoTrabajoJpaEntity::getId).toList();
    }

    @Override
    public Map<UUID, List<UUID>> fotosDeTodos() {
        Map<UUID, List<UUID>> resultado = new LinkedHashMap<>();
        for (FotoTrabajoJpaEntity f : fotos.todas()) {
            resultado.computeIfAbsent(f.getTrabajoId(), k -> new ArrayList<>()).add(f.getId());
        }
        return resultado;
    }

    @Override
    public Optional<UUID> trabajoDeLaFoto(UUID foto) {
        return fotos.buscarPorId(foto).map(FotoTrabajoJpaEntity::getTrabajoId);
    }

    /** Primero la fila de la foto y después el archivo (que la referencia): flush entre medio, como en productos. */
    @Override
    public UUID agregarFoto(UUID trabajo, ImagenLista grande, ImagenLista chica) {
        trabajos.flush();
        UUID id = UUID.randomUUID();
        int posicion = fotos.deTrabajo(trabajo).size();
        fotos.save(new FotoTrabajoJpaEntity(id, trabajo, posicion, grande.ancho(), grande.alto(), reloj.instant()));
        fotos.flush();
        archivos.save(new ArchivoFotoTrabajoJpaEntity(id, grande.jpeg(), chica.jpeg()));
        return id;
    }

    /** Al revés que al agregar: primero el archivo, después la foto. Y se reacomodan las posiciones. */
    @Override
    public void borrarFoto(UUID foto) {
        Optional<FotoTrabajoJpaEntity> fila = fotos.buscarPorId(foto);
        if (fila.isEmpty()) {
            return;
        }
        UUID trabajo = fila.get().getTrabajoId();
        // Se borra lo que se encontró (con el filtro de taller), nunca "por id a ciegas".
        archivos.buscarPorId(foto).ifPresent(archivos::delete);
        archivos.flush();
        fotos.delete(fila.get());
        fotos.flush();
        List<FotoTrabajoJpaEntity> restantes = fotos.deTrabajo(trabajo);
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).moverA(i);
        }
    }

    @Override
    public void ordenarFotos(UUID trabajo, List<UUID> nuevoOrden) {
        for (FotoTrabajoJpaEntity f : fotos.deTrabajo(trabajo)) {
            f.moverA(nuevoOrden.indexOf(f.getId()));
        }
    }

    @Override
    public Optional<byte[]> archivo(UUID foto, boolean chica) {
        return archivos.buscarPorId(foto).map(a -> chica ? a.getChica() : a.getGrande());
    }

    private static Trabajo aDominio(TrabajoJpaEntity e) {
        return new Trabajo(e.getId(), e.getSlug(), e.getCreadoEn(), e.getTipo(), e.getTitulo(),
                new Barrio(e.getBarrio(), e.getBarrioClave()), e.isADomicilio(), e.getProblema(), e.getDiagnostico(),
                e.getSolucion(), e.getVideo(), e.getFecha(), e.getNumeroDeOrden(), e.isConsentimiento(), e.isPublicado());
    }
}
