package com.ram.usuarios.aplicacion;

import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.dominio.CorreoDeAcceso;
import com.ram.usuarios.dominio.CuentaCliente;
import com.ram.usuarios.dominio.Personal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Implementaciones falsas de los puertos, para testear los casos de uso sin base ni BCrypt. */
final class DoblesDeAcceso {

    private DoblesDeAcceso() {
    }

    /** "Cifra" agregando un prefijo: instantáneo y fácil de leer en un test. */
    static class CifradorFalso implements CifradorDeClaves {
        @Override
        public String cifrar(String claveEnClaro) {
            return "cifrada:" + claveEnClaro;
        }

        @Override
        public boolean coincide(String claveEnClaro, String claveCifrada) {
            return claveCifrada.equals(cifrar(claveEnClaro));
        }
    }

    static class PersonalEnMemoria implements PersonalRepositorio {
        final Map<UsuarioId, Personal> datos = new LinkedHashMap<>();

        @Override
        public void guardar(Personal personal) {
            datos.put(personal.id(), personal);
        }

        @Override
        public Optional<Personal> buscarPorId(UsuarioId id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public Optional<Personal> buscarPorCorreo(CorreoDeAcceso correo) {
            return datos.values().stream().filter(p -> p.correo().equals(correo)).findFirst();
        }

        @Override
        public boolean existeCorreo(CorreoDeAcceso correo) {
            return buscarPorCorreo(correo).isPresent();
        }

        @Override
        public boolean hayAlguno() {
            return !datos.isEmpty();
        }
    }

    static class CuentasEnMemoria implements CuentasClientesRepositorio {
        final Map<UsuarioId, CuentaCliente> datos = new LinkedHashMap<>();

        @Override
        public void guardar(CuentaCliente cuenta) {
            datos.put(cuenta.id(), cuenta);
        }

        @Override
        public Optional<CuentaCliente> buscarPorId(UsuarioId id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public Optional<CuentaCliente> buscarPorCorreo(CorreoDeAcceso correo) {
            return datos.values().stream().filter(c -> c.correo().equals(correo)).findFirst();
        }

        @Override
        public boolean existeCorreo(CorreoDeAcceso correo) {
            return buscarPorCorreo(correo).isPresent();
        }

        @Override
        public void borrar(UsuarioId id) {
            datos.remove(id);
        }
    }

    static class RecuperacionesEnMemoria implements RecuperacionesRepositorio {
        final Map<java.util.UUID, com.ram.usuarios.dominio.Recuperacion> datos = new LinkedHashMap<>();

        @Override
        public void guardar(com.ram.usuarios.dominio.Recuperacion recuperacion) {
            datos.put(recuperacion.id(), recuperacion);
        }

        @Override
        public Optional<com.ram.usuarios.dominio.Recuperacion> porHash(String tokenHash) {
            return datos.values().stream().filter(r -> r.tokenHash().equals(tokenHash)).findFirst();
        }

        @Override
        public java.util.List<com.ram.usuarios.dominio.Recuperacion> sinUsarDe(UsuarioId cuenta) {
            return datos.values().stream().filter(r -> r.cuenta().equals(cuenta) && r.usadaEn() == null).toList();
        }
    }
}
