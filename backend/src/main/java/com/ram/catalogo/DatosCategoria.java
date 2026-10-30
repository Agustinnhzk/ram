package com.ram.catalogo;

/** @param conInstalacion si los productos se instalan a domicilio (cámaras, alarmas) */
public record DatosCategoria(String slug, String nombre, boolean conInstalacion) {
}
