# ADR-004: Spring Modulith para verificar los límites entre módulos

- **Estado:** Aceptada
- **Fecha:** 2026-10-27

## Contexto

El ADR-001 define un monolito modular con tres reglas: un módulo solo usa la API pública de otro, no lee tablas ajenas, y reacciona a otros módulos mediante eventos. Esas reglas, si dependen solo de la disciplina, se rompen en el primer apuro.

Necesitamos que una violación haga fallar el build, y una forma estándar de publicar y escuchar eventos entre módulos.

## Decisión

Usar **Spring Modulith**.

- Cada paquete directo bajo `com.ram` es un módulo (`com.ram.tenants`, `com.ram.clientes`, …).
- Lo que está directamente en el paquete del módulo es su API pública. Los subpaquetes (`dominio`, `aplicacion`, `adaptadores`) son internos.
- Un test (`ModulosTests`) verifica en cada build que ningún módulo use internos de otro y que no haya dependencias circulares.
- Los eventos entre módulos se publican y escuchan con el soporte de eventos de Spring Modulith.

## Alternativas consideradas

**ArchUnit.** Librería de tests de arquitectura, muy flexible. Se descartó porque solo verifica: los eventos entre módulos habría que resolverlos aparte, y las reglas de módulos habría que escribirlas a mano.

**Módulos Maven separados** (un proyecto Maven por módulo). El compilador impide las dependencias prohibidas, pero multiplica la configuración del build. Excesivo para este tamaño.

## Consecuencias

**A favor:**

- Las reglas del ADR-001 se verifican solas en cada build.
- Eventos entre módulos con una herramienta pensada para eso.
- Puede generar documentación y diagramas de los módulos a partir del código.

**En contra (costos que aceptamos):**

- La estructura de paquetes queda atada a la convención de Spring Modulith.
- Dentro de un módulo, la separación entre capas hexagonales no la verifica Modulith; se cuida en code review. Si hiciera falta, se puede sumar ArchUnit solo para eso.
