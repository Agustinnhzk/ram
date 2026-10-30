package com.ram.pedidos.aplicacion;

/**
 * Puerto de salida para avisar "pasó algo" a quien le interese (por ahora, el aviso por mail).
 * El servicio no sabe quién escucha.
 */
public interface PublicadorDeEventos {

    void publicar(Object evento);
}
