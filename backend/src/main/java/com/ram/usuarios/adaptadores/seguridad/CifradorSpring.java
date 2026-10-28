package com.ram.usuarios.adaptadores.seguridad;

import com.ram.usuarios.aplicacion.CifradorDeClaves;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador: implementa el puerto del caso de uso con el cifrador de Spring Security (BCrypt). */
@Component
class CifradorSpring implements CifradorDeClaves {

    private final PasswordEncoder encoder;

    CifradorSpring(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String cifrar(String claveEnClaro) {
        return encoder.encode(claveEnClaro);
    }

    @Override
    public boolean coincide(String claveEnClaro, String claveCifrada) {
        return encoder.matches(claveEnClaro, claveCifrada);
    }
}
