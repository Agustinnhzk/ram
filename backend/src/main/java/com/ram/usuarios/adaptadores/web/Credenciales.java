package com.ram.usuarios.adaptadores.web;

/** Lo que llega en un login. El toString oculta la clave, por si algún log imprime este objeto. */
record Credenciales(String correo, String clave) {

    @Override
    public String toString() {
        return "Credenciales[correo=" + correo + ", clave=****]";
    }
}
