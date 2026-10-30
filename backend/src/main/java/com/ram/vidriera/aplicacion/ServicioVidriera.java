package com.ram.vidriera.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.Imagenes;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;
import com.ram.vidriera.BannerNoEncontradoException;
import com.ram.vidriera.BannerNuevo;
import com.ram.vidriera.DatosBanner;
import com.ram.vidriera.SinPermisoException;
import com.ram.vidriera.Vidriera;
import com.ram.vidriera.dominio.Banner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Los banners de la portada (ADR-013). */
public class ServicioVidriera implements Vidriera {

    static final int BANNERS_MAXIMOS = 12;
    /** Los flyers son cuadrados de 1080 px: con 1200 alcanza para cualquier pantalla. */
    static final int LADO_MAXIMO = 1200;

    private final BannersRepositorio banners;
    private final SesionActual sesion;
    private final Clock reloj;
    private final ZoneId zona;

    public ServicioVidriera(BannersRepositorio banners, SesionActual sesion, Clock reloj, ZoneId zona) {
        this.banners = Objects.requireNonNull(banners);
        this.sesion = Objects.requireNonNull(sesion);
        this.reloj = Objects.requireNonNull(reloj);
        this.zona = Objects.requireNonNull(zona);
    }

    @Override
    public List<DatosBanner> paraLaTienda() {
        LocalDate hoy = hoy();
        return banners.todos().stream().filter(b -> b.visible(hoy)).map(b -> aDatos(b, hoy)).toList();
    }

    @Override
    public List<DatosBanner> todos() {
        LocalDate hoy = hoy();
        return banners.todos().stream().map(b -> aDatos(b, hoy)).toList();
    }

    @Override
    public UUID crear(BannerNuevo datos, ArchivoSubido imagen) {
        exigirDueno();
        List<Banner> actuales = banners.todos();
        if (actuales.size() >= BANNERS_MAXIMOS) {
            throw new IllegalArgumentException("Hasta " + BANNERS_MAXIMOS + " banners: borrá o reutilizá alguno");
        }
        Objects.requireNonNull(imagen, "Falta la imagen");
        byte[] jpeg = Imagenes.preparar(imagen, LADO_MAXIMO).jpeg();
        Banner nuevo = Banner.nuevo(datos, actuales.size(), reloj.millis());
        banners.guardar(nuevo);
        banners.guardarImagen(nuevo.id(), jpeg);
        return nuevo.id();
    }

    @Override
    public void editar(UUID id, BannerNuevo datos) {
        exigirDueno();
        Banner banner = buscar(id);
        banner.editar(datos);
        banners.guardar(banner);
    }

    @Override
    public void cambiarImagen(UUID id, ArchivoSubido imagen) {
        exigirDueno();
        Banner banner = buscar(id);
        byte[] jpeg = Imagenes.preparar(imagen, LADO_MAXIMO).jpeg();
        banner.nuevaImagen(reloj.millis());
        banners.guardar(banner);
        banners.guardarImagen(id, jpeg);
    }

    @Override
    public void borrar(UUID id) {
        exigirDueno();
        buscar(id);
        banners.borrar(id);
        List<Banner> restantes = banners.todos();
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).moverA(i);
            banners.guardar(restantes.get(i));
        }
    }

    @Override
    public void ordenar(List<UUID> orden) {
        exigirDueno();
        List<Banner> actuales = banners.todos();
        if (orden == null || orden.size() != actuales.size()
                || !new HashSet<>(orden).equals(new HashSet<>(actuales.stream().map(Banner::id).toList()))) {
            throw new IllegalArgumentException("El orden tiene que incluir todos los banners, una vez cada uno");
        }
        for (Banner b : actuales) {
            b.moverA(orden.indexOf(b.id()));
            banners.guardar(b);
        }
    }

    @Override
    public Optional<byte[]> imagen(UUID id) {
        return banners.imagen(id);
    }

    private Banner buscar(UUID id) {
        return banners.porId(id).orElseThrow(() -> new BannerNoEncontradoException(id));
    }

    private void exigirDueno() {
        if (!sesion.requerido().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede cambiar los banners");
        }
    }

    private LocalDate hoy() {
        return LocalDate.ofInstant(reloj.instant(), zona);
    }

    private static DatosBanner aDatos(Banner b, LocalDate hoy) {
        return new DatosBanner(b.id(), b.textoAlternativo(), b.enlace(), b.imagenHeredada(), b.version(), b.activo(),
                b.desde(), b.hasta(), b.orden(), b.visible(hoy));
    }
}
