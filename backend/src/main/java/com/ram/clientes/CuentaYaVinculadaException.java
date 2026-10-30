package com.ram.clientes;

/** Esa cuenta de la tienda ya está vinculada a otra ficha. Responde 409. */
public class CuentaYaVinculadaException extends RuntimeException {

    public CuentaYaVinculadaException(String nombreDeLaOtraFicha) {
        super("Esa cuenta ya está vinculada a la ficha de " + nombreDeLaOtraFicha + ". Desvinculala de ahí primero.");
    }
}
