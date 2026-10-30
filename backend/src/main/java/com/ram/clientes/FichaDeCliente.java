package com.ram.clientes;

import com.ram.usuarios.CuentaDeTienda;

import java.time.Instant;

/**
 * La ficha completa, para el sistema de gestión.
 *
 * @param notas   internas del taller, o null
 * @param cuenta  la cuenta de la tienda vinculada, o null. En el listado viene solo con el id.
 */
public record FichaDeCliente(ClienteId id, String nombreCompleto, String telefono, String correo, String notas,
                             CuentaDeTienda cuenta, Instant creadoEn) {
}
