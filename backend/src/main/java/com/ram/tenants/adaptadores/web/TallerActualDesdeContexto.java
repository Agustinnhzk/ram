package com.ram.tenants.adaptadores.web;

import com.ram.tenants.adaptadores.contexto.ContextoTaller;
import com.ram.tenants.TallerActual;
import com.ram.tenants.TallerId;
import org.springframework.stereotype.Component;

/** Implementación de la API pública del módulo: lee el taller que dejó el filtro. */
@Component
class TallerActualDesdeContexto implements TallerActual {

    @Override
    public TallerId id() {
        return ContextoTaller.actual()
                .orElseThrow(() -> new IllegalStateException("La request actual no tiene un taller asociado"));
    }

    @Override
    public boolean hayTaller() {
        return ContextoTaller.actual().isPresent();
    }
}
