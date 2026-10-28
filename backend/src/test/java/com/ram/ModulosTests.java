package com.ram;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Cumple la promesa del ADR-001: si un módulo usa clases internas de otro,
 * o si dos módulos dependen uno del otro en círculo, este test falla.
 */
class ModulosTests {

    private final ApplicationModules modulos = ApplicationModules.of(RamApplication.class);

    @Test
    void losModulosRespetanSusLimites() {
        modulos.verify();
    }

    @Test
    void mostrarModulosDetectados() {
        // No verifica nada: imprime en consola qué módulos encontró y qué expone cada uno.
        modulos.forEach(System.out::println);
    }
}
