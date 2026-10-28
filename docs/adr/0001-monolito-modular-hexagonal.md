# ADR-001: Monolito modular con arquitectura hexagonal

- **Estado:** Aceptada
- **Fecha:** 2026-10-23

## Contexto

RAM es la web y el sistema de gestión de un taller de reparación de PC y notebooks e instalación de cámaras y alarmas. Lo desarrolla una sola persona, y la idea es venderlo más adelante como SaaS a otros talleres.

El sistema tiene varias áreas con reglas propias (reparaciones, stock, ventas, pagos, instalaciones) que comparten datos: por ejemplo, el stock que se vende en la tienda es el mismo que se usa en las reparaciones.

Necesitamos una estructura que:

- se pueda desarrollar, testear y deployar con poco esfuerzo operativo;
- mantenga separadas las áreas del negocio para que el código no se vuelva un enredo al crecer;
- permita cambiar proveedores externos (pagos, mails, archivos) sin tocar las reglas del negocio.

## Decisión

Un **único backend (monolito) organizado en módulos**, y cada módulo con **arquitectura hexagonal** (puertos y adaptadores).

**Módulos:** `tenants`, `usuarios`, `clientes`, `catalogo`, `stock`, `ventas`, `pagos`, `reparaciones`, `instalaciones`, `presupuestos`, `contenido`, `diagnosticos`, `importacion`.

**Capas dentro de cada módulo:**

| Capa | Qué contiene | Qué puede conocer |
| --- | --- | --- |
| `dominio` | Entidades y reglas del negocio | Nada externo: ni Spring, ni JPA, ni HTTP |
| `aplicacion` | Casos de uso y puertos (interfaces) | El dominio |
| `adaptadores` | Controladores REST, repositorios JPA, clientes de APIs externas | La aplicación y el dominio |

**Reglas entre módulos:**

1. Un módulo solo usa la API pública de otro módulo, nunca sus clases internas.
2. Ningún módulo lee ni escribe las tablas de otro.
3. Cuando un módulo necesita reaccionar a algo de otro (por ejemplo, descontar stock cuando se paga una orden), lo hace escuchando un evento, no llamándolo directamente.

## Alternativas consideradas

**Microservicios.** Cada área como un servicio independiente con su propia base de datos.
Se descartó porque para un taller y un solo desarrollador agrega costos que no se justifican: comunicación por red, transacciones distribuidas, varios deploys, monitoreo de cada servicio. Los beneficios (escalar partes por separado, equipos independientes) no aplican a este tamaño.

**Monolito en capas tradicional** (controller → service → repository por tipo, sin módulos).
Se descartó porque con el tiempo cualquier service termina usando cualquier repository, las áreas se mezclan y separar un módulo después se vuelve muy difícil. Además ata las reglas del negocio a Spring y a la base de datos.

## Consecuencias

**A favor:**

- Un solo deploy, un solo pipeline, un solo lugar donde mirar logs.
- Las reglas del negocio se testean sin levantar Spring ni base de datos.
- Cambiar o sumar un proveedor externo es escribir un adaptador nuevo.
- Si algún módulo necesita escalar aparte en el futuro, sus límites ya están definidos y se puede extraer.

**En contra (costos que aceptamos):**

- Más estructura al principio: interfaces, mapeos entre dominio y persistencia.
- Los límites entre módulos dependen de disciplina. Para no depender solo de eso, se verifican con **tests de arquitectura** que fallan si un módulo usa clases internas de otro.
- Todo el backend escala junto: si una parte necesita más recursos, se agranda el servidor entero.
