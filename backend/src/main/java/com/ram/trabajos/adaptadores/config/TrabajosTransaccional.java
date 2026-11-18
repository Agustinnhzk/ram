package com.ram.trabajos.adaptadores.config;

import com.ram.archivos.ArchivoSubido;
import com.ram.trabajos.DatosTrabajo;
import com.ram.trabajos.TrabajoNuevo;
import com.ram.trabajos.Trabajos;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class TrabajosTransaccional implements Trabajos {

    private final Trabajos delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    TrabajosTransaccional(Trabajos delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public List<DatosTrabajo> publicados() {
        return lectura.execute(e -> delegado.publicados());
    }

    @Override
    public Optional<DatosTrabajo> publicado(String slug) {
        return lectura.execute(e -> delegado.publicado(slug));
    }

    @Override
    public Optional<byte[]> fotoPublica(UUID foto, boolean chica) {
        return lectura.execute(e -> delegado.fotoPublica(foto, chica));
    }

    @Override
    public List<DatosTrabajo> todos() {
        return lectura.execute(e -> delegado.todos());
    }

    @Override
    public Optional<DatosTrabajo> obtener(UUID id) {
        return lectura.execute(e -> delegado.obtener(id));
    }

    @Override
    public List<String> barrios() {
        return lectura.execute(e -> delegado.barrios());
    }

    @Override
    public UUID crear(TrabajoNuevo datos) {
        return escritura.execute(e -> delegado.crear(datos));
    }

    @Override
    public void editar(UUID id, TrabajoNuevo datos) {
        escritura.executeWithoutResult(e -> delegado.editar(id, datos));
    }

    @Override
    public void borrar(UUID id) {
        escritura.executeWithoutResult(e -> delegado.borrar(id));
    }

    @Override
    public UUID agregarFoto(UUID trabajo, ArchivoSubido imagen) {
        return escritura.execute(e -> delegado.agregarFoto(trabajo, imagen));
    }

    @Override
    public void borrarFoto(UUID trabajo, UUID foto) {
        escritura.executeWithoutResult(e -> delegado.borrarFoto(trabajo, foto));
    }

    @Override
    public void ordenarFotos(UUID trabajo, List<UUID> orden) {
        escritura.executeWithoutResult(e -> delegado.ordenarFotos(trabajo, orden));
    }

    @Override
    public Optional<byte[]> foto(UUID foto, boolean chica) {
        return lectura.execute(e -> delegado.foto(foto, chica));
    }
}
