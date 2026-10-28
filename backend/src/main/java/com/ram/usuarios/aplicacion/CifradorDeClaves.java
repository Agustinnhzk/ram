package com.ram.usuarios.aplicacion;

/**
 * Puerto: cifrar contraseñas y comprobarlas. La implementación real usa BCrypt (Spring Security);
 * en los tests se usa una falsa e instantánea.
 *
 * <p>BCrypt es un hash "lento a propósito" y con sal: aunque alguien se lleve la base de datos,
 * probar millones de contraseñas contra cada hash le llevaría años.
 */
public interface CifradorDeClaves {

    String cifrar(String claveEnClaro);

    boolean coincide(String claveEnClaro, String claveCifrada);
}
