package com.ram.clientes.dominio;

import java.util.Objects;

/**
 * Teléfono del cliente, guardado solo con números (y un + inicial si lo tiene).
 * "341 555-1234" y "3415551234" quedan iguales, así se comparan bien y el link de WhatsApp funciona.
 */
public record Telefono(String valor) {

    public Telefono {
        Objects.requireNonNull(valor, "El teléfono es obligatorio");
        String limpio = valor.trim();
        boolean internacional = limpio.startsWith("+");
        String digitos = limpio.replaceAll("[^0-9]", "");
        if (!limpio.matches("\\+?[0-9 ()\\-.]+")) {
            throw new IllegalArgumentException("Teléfono inválido: '" + valor + "'");
        }
        if (digitos.length() < 6 || digitos.length() > 15) {
            throw new IllegalArgumentException("El teléfono tiene que tener entre 6 y 15 números");
        }
        valor = internacional ? "+" + digitos : digitos;
    }
}
