package com.ram.catalogo.adaptadores.persistencia;

import com.ram.archivos.ImagenLista;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.aplicacion.FotosDeProductos;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
class FotosDeProductosJpa implements FotosDeProductos {

    private final FotoProductoJpaRepository fotos;
    private final ArchivoFotoJpaRepository archivos;
    private final Clock reloj = Clock.systemUTC();

    FotosDeProductosJpa(FotoProductoJpaRepository fotos, ArchivoFotoJpaRepository archivos) {
        this.fotos = fotos;
        this.archivos = archivos;
    }

    @Override
    public List<UUID> de(ProductoId producto) {
        return fotos.deProducto(producto.valor()).stream().map(FotoProductoJpaEntity::getId).toList();
    }

    @Override
    public Map<ProductoId, List<UUID>> deTodos() {
        Map<ProductoId, List<UUID>> resultado = new LinkedHashMap<>();
        for (FotoProductoJpaEntity f : fotos.todas()) {
            resultado.computeIfAbsent(ProductoId.de(f.getProductoId()), k -> new java.util.ArrayList<>()).add(f.getId());
        }
        return resultado;
    }

    /** Primero la fila de la foto y después el archivo (que la referencia): flush entre medio, como en los comprobantes. */
    @Override
    public UUID agregar(ProductoId producto, ImagenLista grande, ImagenLista chica) {
        UUID id = UUID.randomUUID();
        int posicion = fotos.deProducto(producto.valor()).size();
        fotos.save(new FotoProductoJpaEntity(id, producto.valor(), posicion, grande.ancho(), grande.alto(), reloj.instant()));
        fotos.flush();
        archivos.save(new ArchivoFotoJpaEntity(id, grande.jpeg(), chica.jpeg()));
        return id;
    }

    /** Al revés que al agregar: primero el archivo (que apunta a la foto), después la foto. Y se reacomodan las posiciones. */
    @Override
    public void borrar(UUID foto) {
        Optional<FotoProductoJpaEntity> fila = fotos.buscarPorId(foto);
        if (fila.isEmpty()) {
            return;
        }
        UUID producto = fila.get().getProductoId();
        // Se borra lo que se encontró (con el filtro de taller), nunca "por id a ciegas".
        archivos.buscarPorId(foto).ifPresent(archivos::delete);
        archivos.flush();
        fotos.delete(fila.get());
        fotos.flush();
        List<FotoProductoJpaEntity> restantes = fotos.deProducto(producto);
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).moverA(i);
        }
    }

    @Override
    public void ordenar(ProductoId producto, List<UUID> nuevoOrden) {
        for (FotoProductoJpaEntity f : fotos.deProducto(producto.valor())) {
            f.moverA(nuevoOrden.indexOf(f.getId()));
        }
    }

    @Override
    public Optional<byte[]> archivo(UUID foto, boolean chica) {
        return archivos.buscarPorId(foto).map(a -> chica ? a.getChica() : a.getGrande());
    }
}
