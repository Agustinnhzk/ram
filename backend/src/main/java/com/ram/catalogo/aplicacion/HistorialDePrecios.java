package com.ram.catalogo.aplicacion;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.dominio.CambioDePrecio;

import java.util.List;

/** Puerto de salida: el historial solo se agrega y se lee, nunca se modifica ni se borra. */
public interface HistorialDePrecios {

    void registrar(CambioDePrecio cambio);

    /** Del más nuevo al más viejo. */
    List<CambioDePrecio> deProducto(ProductoId producto, int limite);
}
