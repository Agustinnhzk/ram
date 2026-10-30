package com.ram.catalogo;

public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(ProductoId id) {
        super("No existe el producto " + id);
    }
}
