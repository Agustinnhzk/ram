package com.ram.catalogo.adaptadores.config;

import com.ram.catalogo.Catalogo;
import com.ram.catalogo.DatosCambioDePrecio;
import com.ram.catalogo.DatosCategoria;
import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.FiltroCatalogo;
import com.ram.catalogo.ProductoId;
import com.ram.catalogo.VistaPreviaDeAumento;
import org.springframework.transaction.support.TransactionTemplate;

import com.ram.archivos.ArchivoSubido;
import com.ram.catalogo.ArchivoDeFoto;
import com.ram.catalogo.FichaDeProducto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Decorador con transacciones, igual que ClientesTransaccionales.
 *
 * <p>Lo importante acá: aplicarAumento cambia N precios y escribe N líneas de historial en UNA
 * transacción. Si falla a la mitad, no queda nada a medias: o se aplica todo, o nada.
 */
class CatalogoTransaccional implements Catalogo {

    private final Catalogo delegado;
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    CatalogoTransaccional(Catalogo delegado, TransactionTemplate escritura, TransactionTemplate lectura) {
        this.delegado = delegado;
        this.escritura = escritura;
        this.lectura = lectura;
    }

    @Override
    public List<DatosCategoria> categorias() {
        return lectura.execute(estado -> delegado.categorias());
    }

    @Override
    public List<DatosProducto> buscar(FiltroCatalogo filtro) {
        return lectura.execute(estado -> delegado.buscar(filtro));
    }

    @Override
    public Optional<DatosProducto> obtenerPublicado(String slug) {
        return lectura.execute(estado -> delegado.obtenerPublicado(slug));
    }

    @Override
    public List<DatosProducto> componentes() {
        return lectura.execute(estado -> delegado.componentes());
    }

    @Override
    public List<DatosProducto> listarParaPrecios() {
        return lectura.execute(estado -> delegado.listarParaPrecios());
    }

    @Override
    public void cambiarPrecio(ProductoId id, Long nuevoPrecio) {
        escritura.executeWithoutResult(estado -> delegado.cambiarPrecio(id, nuevoPrecio));
    }

    @Override
    public VistaPreviaDeAumento simularAumento(BigDecimal porcentaje, String categoria) {
        return lectura.execute(estado -> delegado.simularAumento(porcentaje, categoria));
    }

    @Override
    public int aplicarAumento(BigDecimal porcentaje, String categoria) {
        Integer cambiados = escritura.execute(estado -> delegado.aplicarAumento(porcentaje, categoria));
        return cambiados == null ? 0 : cambiados;
    }

    @Override
    public List<DatosCambioDePrecio> historialDePrecios(ProductoId id) {
        return lectura.execute(estado -> delegado.historialDePrecios(id));
    }

    @Override
    public Optional<DatosProducto> obtener(ProductoId id) {
        return lectura.execute(estado -> delegado.obtener(id));
    }

    @Override
    public ProductoId crearProducto(FichaDeProducto ficha, Long precio) {
        return escritura.execute(estado -> delegado.crearProducto(ficha, precio));
    }

    @Override
    public void editarProducto(ProductoId id, FichaDeProducto ficha) {
        escritura.executeWithoutResult(estado -> delegado.editarProducto(id, ficha));
    }

    @Override
    public void cambiarOferta(ProductoId id, Long precioOferta, LocalDate hasta) {
        escritura.executeWithoutResult(estado -> delegado.cambiarOferta(id, precioOferta, hasta));
    }

    @Override
    public UUID agregarFoto(ProductoId id, ArchivoSubido imagen) {
        return escritura.execute(estado -> delegado.agregarFoto(id, imagen));
    }

    @Override
    public void borrarFoto(ProductoId id, UUID foto) {
        escritura.executeWithoutResult(estado -> delegado.borrarFoto(id, foto));
    }

    @Override
    public void ordenarFotos(ProductoId id, List<UUID> fotos) {
        escritura.executeWithoutResult(estado -> delegado.ordenarFotos(id, fotos));
    }

    @Override
    public Optional<ArchivoDeFoto> foto(UUID foto, boolean chica) {
        return lectura.execute(estado -> delegado.foto(foto, chica));
    }

    @Override
    public DatosCategoria crearCategoria(String nombre, boolean conInstalacion) {
        return escritura.execute(estado -> delegado.crearCategoria(nombre, conInstalacion));
    }

    @Override
    public void editarCategoria(String slug, String nombre, boolean conInstalacion) {
        escritura.executeWithoutResult(estado -> delegado.editarCategoria(slug, nombre, conInstalacion));
    }
}
