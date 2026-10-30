package com.ram.vidriera.adaptadores.persistencia;

import com.ram.vidriera.aplicacion.BannersRepositorio;
import com.ram.vidriera.dominio.Banner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class BannersRepositorioJpa implements BannersRepositorio {

    private final BannerJpaRepository banners;
    private final ImagenBannerJpaRepository imagenes;

    BannersRepositorioJpa(BannerJpaRepository banners, ImagenBannerJpaRepository imagenes) {
        this.banners = banners;
        this.imagenes = imagenes;
    }

    @Override
    public List<Banner> todos() {
        return banners.todos().stream().map(BannersRepositorioJpa::aDominio).toList();
    }

    @Override
    public Optional<Banner> porId(UUID id) {
        return banners.buscarPorId(id).map(BannersRepositorioJpa::aDominio);
    }

    @Override
    public void guardar(Banner b) {
        BannerJpaEntity entidad = banners.buscarPorId(b.id()).orElseGet(() -> new BannerJpaEntity(b.id()));
        entidad.actualizar(b.textoAlternativo(), b.enlace(), b.imagenHeredada(), b.version(), b.activo(), b.desde(), b.hasta(), b.orden());
        banners.save(entidad);
    }

    /** La imagen apunta al banner (FOREIGN KEY): flush antes, para que el banner nuevo ya esté insertado. */
    @Override
    public void guardarImagen(UUID banner, byte[] jpeg) {
        banners.flush();
        ImagenBannerJpaEntity imagen = imagenes.buscarPorId(banner).orElseGet(() -> new ImagenBannerJpaEntity(banner));
        imagen.reemplazar(jpeg);
        imagenes.save(imagen);
    }

    @Override
    public Optional<byte[]> imagen(UUID banner) {
        return imagenes.buscarPorId(banner).map(ImagenBannerJpaEntity::getContenido);
    }

    @Override
    public void borrar(UUID banner) {
        imagenes.buscarPorId(banner).ifPresent(imagenes::delete);
        imagenes.flush();
        banners.buscarPorId(banner).ifPresent(banners::delete);
        banners.flush();
    }

    private static Banner aDominio(BannerJpaEntity e) {
        return new Banner(e.getId(), e.getTextoAlternativo(), e.getEnlace(), e.getImagenHeredada(), e.getImagenVersion(),
                e.isActivo(), e.getDesde(), e.getHasta(), e.getOrden());
    }
}
