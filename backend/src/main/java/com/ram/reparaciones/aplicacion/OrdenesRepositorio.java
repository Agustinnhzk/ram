package com.ram.reparaciones.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.dominio.OrdenDeReparacion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/** Puerto de salida. Todo es, implícitamente, del taller actual. Las órdenes vienen con su bitácora. */
public interface OrdenesRepositorio {

    /** Crea la orden o guarda sus cambios, y agrega a la bitácora los movimientos nuevos. */
    void guardar(OrdenDeReparacion orden);

    Optional<OrdenDeReparacion> porId(OrdenId id);

    Optional<OrdenDeReparacion> porNumero(int numero);

    boolean existeNumero(int numero);

    OptionalInt ultimoNumero();

    /** Las más nuevas primero (por número). */
    List<OrdenDeReparacion> conEstados(Set<EstadoReparacion> estados, int limite);

    /** Las más nuevas primero (por número). */
    List<OrdenDeReparacion> deClientes(Collection<ClienteId> clientes, int limite);
}
