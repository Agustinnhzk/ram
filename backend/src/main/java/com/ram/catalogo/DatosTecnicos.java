package com.ram.catalogo;

import java.util.List;

/**
 * Lo que hace falta para la compatibilidad básica de Armá tu PC. Cada tipo usa sus campos:
 * CPU (socket, consumoW, graficosIntegrados), MOTHER (socket, tipoRam, formato), RAM (tipoRam),
 * GPU (consumoW), FUENTE (potenciaW), GABINETE (formatosSoportados). El resto queda en null.
 */
public record DatosTecnicos(
        TipoComponente tipo,
        String socket,
        TipoRam tipoRam,
        FormatoMother formato,
        List<FormatoMother> formatosSoportados,
        Integer consumoW,
        Integer potenciaW,
        boolean graficosIntegrados) {

    public DatosTecnicos {
        formatosSoportados = formatosSoportados == null ? List.of() : List.copyOf(formatosSoportados);
    }
}
