package com.ram.usuarios;

import java.util.UUID;

/** Lo que otros módulos ven de una cuenta de la tienda: sin la clave, claro. */
public record CuentaDeTienda(UUID id, String nombre, String correo) {
}
