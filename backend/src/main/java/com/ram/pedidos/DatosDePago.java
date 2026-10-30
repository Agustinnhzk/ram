package com.ram.pedidos;

/** A dónde transferir. @param cbu null si el taller no lo cargó */
public record DatosDePago(String alias, String titular, String entidad, String cbu) {
}
