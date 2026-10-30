package com.ram.instalaciones;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de entrada del módulo. Todo es del taller actual. Lo usa todo el personal; marcar pagos: Dueño y Mostrador. */
public interface Instalaciones {

    /** @param terminadas false = las que están en curso; true = las terminadas */
    List<Instalacion> listar(boolean terminadas);

    Optional<Instalacion> obtener(UUID id);

    /** El número es correlativo (el último + 1). */
    Instalacion crear(DatosDeInstalacion datos);

    /** El cliente no cambia. */
    void editar(UUID id, DatosDeInstalacion datos);

    // ─── Monitoreo ───────────────────────────────────────────────────────────────────────────

    /** Todos los abonados con los pagos de ese año, por nombre. */
    List<FilaDeAbonos> abonos(int anio);

    UUID agregarAbonado(DatosDeAbonado datos);

    void editarAbonado(UUID id, DatosDeAbonado datos);

    /** @param mes 1 = enero … 12 = diciembre */
    void marcarPago(UUID abonado, int anio, int mes, boolean pagado);
}
