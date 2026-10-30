package com.ram.instalaciones.aplicacion;

import com.ram.instalaciones.dominio.Abonado;
import com.ram.instalaciones.dominio.FichaDeInstalacion;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Puerto de salida. Todo es del taller actual (ADR-005). */
public interface InstalacionesRepositorio {

    /** Crea o actualiza. */
    void guardar(FichaDeInstalacion instalacion);

    Optional<FichaDeInstalacion> instalacion(UUID id);

    /** Las más nuevas primero. */
    List<FichaDeInstalacion> instalaciones(boolean terminadas);

    int ultimoNumero();

    void guardar(Abonado abonado);

    Optional<Abonado> abonado(UUID id);

    List<Abonado> abonados();

    /** Los meses pagados de ese año: "abonado:mes". */
    Set<String> pagos(int anio);

    void marcarPago(UUID abonado, int anio, int mes, boolean pagado);
}
