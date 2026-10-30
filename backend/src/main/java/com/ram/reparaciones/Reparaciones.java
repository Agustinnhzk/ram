package com.ram.reparaciones;

import com.ram.clientes.ClienteId;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** Puerto de entrada del módulo. Todo es del taller de la request actual. */
public interface Reparaciones {

    /** El número de orden más alto usado hasta ahora, como ayuda para cargar el siguiente. */
    OptionalInt ultimoNumero();

    /**
     * @throws NumeroDeOrdenRepetidoException si ese número ya existe en el taller
     * @throws IllegalArgumentException       si falta algo o algún dato es inválido
     */
    OrdenId registrarIngreso(NuevoIngreso ingreso);

    /** @param texto opcional: un número de orden, o parte del nombre del cliente */
    List<ResumenDeOrden> listar(VistaDeOrdenes vista, String texto);

    Optional<DetalleDeOrden> obtener(OrdenId id);

    /** Todas las órdenes de un cliente (para su ficha), de la más nueva a la más vieja. */
    List<ResumenDeOrden> deCliente(ClienteId cliente);

    /**
     * Para el cliente logueado en la tienda: sus reparaciones, si su cuenta está vinculada a su ficha (ADR-014).
     * Muestran lo mismo que el seguimiento: nada interno del taller.
     */
    List<Seguimiento> misReparaciones();

    /**
     * Para los cambios de estado que no necesitan datos extra. Presupuestar y entregar tienen
     * sus propios métodos, porque piden el monto y la garantía.
     *
     * @param notaParaCliente si la nota (y el cambio) se ven en el seguimiento
     * @throws TransicionInvalidaException si el recorrido no permite ese paso
     */
    void cambiarEstado(OrdenId id, EstadoReparacion destino, String nota, boolean notaParaCliente);

    /** Carga (o corrige) el presupuesto y deja la orden en PRESUPUESTADO. */
    void presupuestar(OrdenId id, long monto, String descripcion);

    /** La salida: el equipo se entrega con la garantía de ESA reparación (0 = sin garantía). */
    void entregar(OrdenId id, int garantiaDias, String nota);

    void agregarNota(OrdenId id, String texto, boolean paraCliente);

    /**
     * Para el cliente, sin login: necesita el número y los últimos 4 dígitos del teléfono.
     * Vacío si no coinciden (no dice cuál de los dos estuvo mal).
     */
    Optional<Seguimiento> seguimiento(int numero, String ultimosDigitosDelTelefono);
}
