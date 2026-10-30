# ADR-003: Tienda y sistema de gestión como aplicaciones separadas

- **Estado:** Aceptada (la elección de Next.js para el sistema fue reemplazada por el [ADR-007](0007-sistema-de-gestion-con-react-y-vite.md))
- **Fecha:** 2026-10-27

## Contexto

RAM tiene dos públicos muy distintos:

- **Clientes**: navegan la tienda y los servicios sin loguearse, y se registran solo para comprar.
- **Personal del taller**: usa el sistema de gestión para registrar ingresos, diagnósticos, presupuestos, reparaciones, salidas, instalaciones y monitoreos.

Si los dos conviven en la misma aplicación, comparten sesión, cookies y deploy: un error o una vulnerabilidad en la tienda podría afectar al sistema, y no se puede actualizar uno sin el otro.

## Decisión

Dos aplicaciones frontend separadas, cada una en su subdominio, con un único backend:

| Aplicación | Dirección (ejemplo) | Quién entra | Login |
| --- | --- | --- | --- |
| Tienda (web pública) | `ramservicios.com.ar` | Cualquiera | Solo para comprar (cliente) |
| Sistema de gestión | `sistema.ramservicios.com.ar` | Personal del taller | Siempre (personal) |

~~Las dos usan el mismo stack (Next.js), y los componentes en común viven en una carpeta compartida del repositorio.~~
Ver ADR-007: la tienda usa Next.js y el sistema, React + Vite.

El backend separa sus rutas por canal:

| Ruta | Para qué | Acceso |
| --- | --- | --- |
| `/api/publico` | Catálogo, servicios, contenido | Sin login |
| `/api/tienda` | Carrito, compra, mis compras | Cliente logueado |
| `/api/sistema` | Gestión del taller | Personal logueado |

**Cliente y personal son tipos de usuario distintos.** Un cliente no puede acceder a `/api/sistema` aunque conozca la URL.

## Alternativas consideradas

**Misma aplicación con ruta protegida** (`/panel`). Un solo proyecto, pero sesión, cookies y deploy compartidos. Descartada por seguridad y por acoplar los deploys.

**Misma aplicación servida en otro subdominio.** Separa las sesiones, pero sigue siendo un solo código y un solo deploy. Descartada por quedarse a mitad de camino.

## Consecuencias

**A favor:**

- Sesiones, deploys y fallas independientes.
- Se puede actualizar el sistema sin tocar la tienda, y al revés.
- Más adelante se puede restringir el acceso al sistema (por ejemplo, solo desde la red del taller) sin afectar la tienda.
- La tienda se optimiza para SEO y velocidad; el sistema, para trabajo diario.

**En contra (costos que aceptamos):**

- Dos proyectos frontend que mantener y deployar.
- Hay que cuidar que los componentes compartidos no se dupliquen.
