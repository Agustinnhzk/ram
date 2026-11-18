package com.ram.trabajos.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.ImagenLista;
import com.ram.archivos.Imagenes;
import com.ram.trabajos.DatosTrabajo;
import com.ram.trabajos.SinPermisoException;
import com.ram.trabajos.TrabajoNoEncontradoException;
import com.ram.trabajos.TrabajoNuevo;
import com.ram.trabajos.Trabajos;
import com.ram.trabajos.dominio.Trabajo;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Los trabajos realizados (ADR-018). Cargar, editar, publicar y las fotos: solo el Dueño. */
public class ServicioTrabajos implements Trabajos {

    static final int FOTOS_MAXIMAS = 8;
    static final int LADO_GRANDE = 1600;
    static final int LADO_CHICA = 600;

    private final TrabajosRepositorio trabajos;
    private final SesionActual sesion;
    private final Clock reloj;
    private final ZoneId zona;

    public ServicioTrabajos(TrabajosRepositorio trabajos, SesionActual sesion, Clock reloj, ZoneId zona) {
        this.trabajos = Objects.requireNonNull(trabajos);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
        this.zona = Objects.requireNonNull(zona);
    }

    // ─── Tienda ──────────────────────────────────────────────────────────────────────────────

    @Override
    public List<DatosTrabajo> publicados() {
        Map<UUID, List<UUID>> fotos = trabajos.fotosDeTodos();
        return trabajos.todos().stream().filter(Trabajo::publicado)
                .map(t -> aDatos(t, fotos.getOrDefault(t.id(), List.of()))).toList();
    }

    @Override
    public Optional<DatosTrabajo> publicado(String slug) {
        return trabajos.porSlug(slug).filter(Trabajo::publicado).map(this::conFotos);
    }

    @Override
    public Optional<byte[]> fotoPublica(UUID foto, boolean chica) {
        boolean deUnPublicado = trabajos.trabajoDeLaFoto(foto).flatMap(trabajos::porId).map(Trabajo::publicado).orElse(false);
        return deUnPublicado ? trabajos.archivo(foto, chica) : Optional.empty();
    }

    // ─── Sistema ─────────────────────────────────────────────────────────────────────────────

    @Override
    public List<DatosTrabajo> todos() {
        Map<UUID, List<UUID>> fotos = trabajos.fotosDeTodos();
        return trabajos.todos().stream().map(t -> aDatos(t, fotos.getOrDefault(t.id(), List.of()))).toList();
    }

    @Override
    public Optional<DatosTrabajo> obtener(UUID id) {
        return trabajos.porId(id).map(this::conFotos);
    }

    /** Uno por zona ("alberdi"), como se escribió en el trabajo más reciente. Ordenados alfabéticamente. */
    @Override
    public List<String> barrios() {
        Map<String, String> porClave = new LinkedHashMap<>();
        for (Trabajo t : trabajos.todos()) {
            porClave.putIfAbsent(t.barrio().clave(), t.barrio().texto());
        }
        return porClave.values().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    @Override
    public UUID crear(TrabajoNuevo datos) {
        exigirDueno();
        Trabajo nuevo = Trabajo.nuevo(datos, hoy(), reloj.instant(), trabajos::existeSlug);
        trabajos.guardar(nuevo);
        return nuevo.id();
    }

    @Override
    public void editar(UUID id, TrabajoNuevo datos) {
        exigirDueno();
        Trabajo trabajo = buscar(id);
        trabajo.editar(datos, hoy());
        trabajos.guardar(trabajo);
    }

    @Override
    public void borrar(UUID id) {
        exigirDueno();
        buscar(id);
        trabajos.borrar(id);
    }

    @Override
    public UUID agregarFoto(UUID trabajo, ArchivoSubido imagen) {
        exigirDueno();
        buscar(trabajo);
        Objects.requireNonNull(imagen, "Falta la foto");
        if (trabajos.fotosDe(trabajo).size() >= FOTOS_MAXIMAS) {
            throw new IllegalArgumentException("Cada trabajo puede tener hasta " + FOTOS_MAXIMAS + " fotos");
        }
        // Preparar la foto la vuelve a guardar como JPEG sin el EXIF: no queda la ubicación GPS del celular.
        ImagenLista grande = Imagenes.preparar(imagen, LADO_GRANDE);
        ImagenLista chica = Imagenes.preparar(imagen, LADO_CHICA);
        return trabajos.agregarFoto(trabajo, grande, chica);
    }

    @Override
    public void borrarFoto(UUID trabajo, UUID foto) {
        exigirDueno();
        buscar(trabajo);
        if (!trabajos.fotosDe(trabajo).contains(foto)) {
            throw new IllegalArgumentException("Esa foto no es de este trabajo");
        }
        trabajos.borrarFoto(foto);
    }

    @Override
    public void ordenarFotos(UUID trabajo, List<UUID> orden) {
        exigirDueno();
        buscar(trabajo);
        List<UUID> actuales = trabajos.fotosDe(trabajo);
        if (orden == null || orden.size() != actuales.size() || !new HashSet<>(orden).equals(new HashSet<>(actuales))) {
            throw new IllegalArgumentException("El orden tiene que incluir todas las fotos del trabajo, una vez cada una");
        }
        trabajos.ordenarFotos(trabajo, orden);
    }

    @Override
    public Optional<byte[]> foto(UUID foto, boolean chica) {
        return trabajos.archivo(foto, chica);
    }

    // ─── Ayudas ──────────────────────────────────────────────────────────────────────────────

    private Trabajo buscar(UUID id) {
        return trabajos.porId(id).orElseThrow(() -> new TrabajoNoEncontradoException(String.valueOf(id)));
    }

    private void exigirDueno() {
        if (!sesion.requerido().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede cargar y publicar trabajos");
        }
    }

    private LocalDate hoy() {
        return LocalDate.ofInstant(reloj.instant(), zona);
    }

    private DatosTrabajo conFotos(Trabajo t) {
        return aDatos(t, trabajos.fotosDe(t.id()));
    }

    private static DatosTrabajo aDatos(Trabajo t, List<UUID> fotos) {
        return new DatosTrabajo(t.id(), t.slug(), t.tipo(), t.titulo(), t.barrio().texto(), t.barrio().clave(), t.aDomicilio(),
                t.problema(), t.diagnostico(), t.solucion(), t.fecha(), t.numeroDeOrden(), t.consentimiento(), t.publicado(),
                List.copyOf(fotos));
    }
}
