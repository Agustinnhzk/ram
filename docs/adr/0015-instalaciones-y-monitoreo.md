# ADR-015: Instalaciones y monitoreo como registro simple

- **Estado:** Aceptada
- **Fecha:** 2026-10-30

## Contexto

El dueño quiere **tener registradas** las instalaciones de cámaras y alarmas y los abonos de monitoreo, sin circuitos
complicados: "es más para tenerlo registrado solamente".

## Decisión

Un módulo `instalaciones` con dos partes, sobre las mismas fichas de `clientes` (ADR-014):

**Instalaciones.** Número correlativo, cliente (existente o nuevo con nombre y teléfono), dirección, estado, equipos,
cantidad de cámaras, fecha agendada, técnico, presupuesto y notas (por ejemplo, el costo de la visita según la distancia).
Estados: **Visita → Presupuesto → Instalación → Terminada**, y se puede pasar de cualquiera a cualquiera: es un
registro, no un circuito con reglas. "Terminada" existe para que el listado del día no crezca para siempre.

**Monitoreo.** Abonados (cliente, dirección, abono mensual, activo) y una planilla de **enero a diciembre por año**
con Sí/No. En la base, una fila de `pagos_abono` = pagó ese mes; "No" = no hay fila. Marcar pagos: Dueño y Mostrador.

## Alternativas consideradas

- **Copiar el circuito de reparaciones** (bitácora, transiciones válidas, comprobante): más de lo que hace falta hoy.
- **Generar cuotas y registrar montos pagados:** útil si algún día hay débito automático o pagos parciales.

## Consecuencias

**A favor:** se carga en segundos; la planilla se lee como el Excel de siempre.

**En contra:** no hay historial de cambios de cada instalación ni fotos del trabajo; no se registra cuánto pagó cada
mes, solo si pagó. Las dos cosas se pueden sumar sin cambiar lo que ya está.
