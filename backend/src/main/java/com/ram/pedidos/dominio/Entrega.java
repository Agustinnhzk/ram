package com.ram.pedidos.dominio;

import com.ram.pedidos.FormaDeEntrega;

import java.util.Objects;

/** Cómo recibe el pedido. Con envío, la dirección es obligatoria; con retiro, no se guarda. */
public record Entrega(FormaDeEntrega forma, String direccion) {

    public Entrega {
        Objects.requireNonNull(forma, "Falta la forma de entrega");
        direccion = forma.esEnvio() ? Texto.obligatorio(direccion, "la dirección de entrega", 300) : null;
    }

    public static Entrega de(FormaDeEntrega forma, String direccion) {
        if (forma == null) {
            throw new IllegalArgumentException("Elegí si lo retirás o te lo enviamos");
        }
        return new Entrega(forma, direccion);
    }
}
