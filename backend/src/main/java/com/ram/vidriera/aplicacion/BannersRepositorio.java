package com.ram.vidriera.aplicacion;

import com.ram.vidriera.dominio.Banner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BannersRepositorio {

    /** En orden. */
    List<Banner> todos();

    Optional<Banner> porId(UUID id);

    void guardar(Banner banner);

    /** Guarda o reemplaza la imagen (JPEG) de un banner ya guardado. */
    void guardarImagen(UUID banner, byte[] jpeg);

    Optional<byte[]> imagen(UUID banner);

    void borrar(UUID banner);
}
