package com.ram.vidriera.adaptadores.config;

import com.ram.archivos.ArchivoSubido;
import com.ram.vidriera.BannerNuevo;
import com.ram.vidriera.DatosBanner;
import com.ram.vidriera.Vidriera;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class VidrieraTransaccional implements Vidriera {

    private final Vidriera delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    VidrieraTransaccional(Vidriera delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public List<DatosBanner> paraLaTienda() {
        return lectura.execute(e -> delegado.paraLaTienda());
    }

    @Override
    public List<DatosBanner> todos() {
        return lectura.execute(e -> delegado.todos());
    }

    @Override
    public UUID crear(BannerNuevo datos, ArchivoSubido imagen) {
        return escritura.execute(e -> delegado.crear(datos, imagen));
    }

    @Override
    public void editar(UUID id, BannerNuevo datos) {
        escritura.executeWithoutResult(e -> delegado.editar(id, datos));
    }

    @Override
    public void cambiarImagen(UUID id, ArchivoSubido imagen) {
        escritura.executeWithoutResult(e -> delegado.cambiarImagen(id, imagen));
    }

    @Override
    public void borrar(UUID id) {
        escritura.executeWithoutResult(e -> delegado.borrar(id));
    }

    @Override
    public void ordenar(List<UUID> orden) {
        escritura.executeWithoutResult(e -> delegado.ordenar(orden));
    }

    @Override
    public Optional<byte[]> imagen(UUID id) {
        return lectura.execute(e -> delegado.imagen(id));
    }
}
