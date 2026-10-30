package com.ram.contactos;

/**
 * Una empresa que quiere tercerizar su IT o hacer una alianza comercial.
 *
 * @param contacto la persona que escribe en nombre de la empresa
 */
public record ConsultaDeEmpresa(String empresa, String contacto, String correo, String telefono,
                                MotivoDeEmpresa motivo, String mensaje) {
}
