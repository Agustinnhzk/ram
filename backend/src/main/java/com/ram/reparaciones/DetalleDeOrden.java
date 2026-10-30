package com.ram.reparaciones;

import com.ram.clientes.DatosCliente;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Todo sobre una orden, para la pantalla del sistema.
 *
 * @param password       null si no tiene, o si quien pregunta no es Dueño ni Técnico (ver passwordVisible)
 * @param passwordVisible false si quien pregunta no puede verlo (Mostrador)
 * @param siguientes     a qué estados se puede pasar ahora: la pantalla muestra solo esos botones
 *                       (PRESUPUESTADO y ENTREGADO abren su formulario, porque piden monto y garantía)
 */
public record DetalleDeOrden(
        OrdenId id,
        int numero,
        DatosCliente cliente,
        String equipo,
        boolean conCargador,
        String procesador,
        String password,
        boolean passwordVisible,
        String problema,
        String observaciones,
        EstadoReparacion estado,
        Set<EstadoReparacion> siguientes,
        Long presupuestoMonto,
        String presupuestoDescripcion,
        Instant ingresadoEn,
        Instant entregadoEn,
        Integer garantiaDias,
        LocalDate garantiaHasta,
        boolean enGarantia,
        List<DatosMovimiento> movimientos) {

    @Override
    public String toString() {
        return "DetalleDeOrden[numero=" + numero + ", estado=" + estado + ", password=" + (password == null ? "no" : "***") + "]";
    }
}
