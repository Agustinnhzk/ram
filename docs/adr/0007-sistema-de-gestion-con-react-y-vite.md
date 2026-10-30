# ADR-007: Sistema de gestión como aplicación React + Vite (SPA)

- **Estado:** Aceptada
- **Fecha:** 2026-10-29
- **Reemplaza en parte a:** [ADR-003](0003-tienda-y-sistema-separados.md) (solo la elección de Next.js para el sistema; la separación en dos aplicaciones se mantiene)

## Contexto

El ADR-003 separó la tienda y el sistema de gestión en dos aplicaciones, y supuso que las dos usarían Next.js.
Al terminar la tienda quedó claro que tienen necesidades opuestas:

| | Tienda | Sistema de gestión |
| --- | --- | --- |
| Usuarios | Cualquiera | Solo personal del taller, siempre con login |
| Buscadores (SEO) | Imprescindible | No debe aparecer |
| Tipo de pantalla | Páginas de contenido | Aplicación: formularios, tablas, acciones |
| Datos | Públicos, cacheables | Privados, cambian todo el tiempo |

Lo que aporta Next.js (HTML generado en el servidor, caché, pre-renderizado) sirve para la tienda y no para el sistema.

Además, la autenticación (ADR-006) ya está diseñada para un cliente que corre en el navegador:
cookie de sesión `SESSION` (HttpOnly) y token CSRF que el JavaScript lee de la cookie `XSRF-TOKEN`
y reenvía en el header `X-XSRF-TOKEN`.

## Decisión

El sistema de gestión es una **SPA (Single Page Application) con React, TypeScript y Vite**, en la carpeta `sistema/` del repositorio:

```
ram/
├── backend/   Spring Boot: API
├── web/       Next.js: tienda pública (y la página de seguimiento de reparaciones para el cliente)
└── sistema/   React + Vite: gestión del taller
```

- Corre entera en el navegador y llama directo a `/api/sistema/**`, en el mismo dominio (`sistema.<dominio>`),
  así la cookie de sesión viaja sola y no hace falta CORS.
- Al publicar es un conjunto de archivos estáticos que sirve el mismo servidor web que hace de proxy hacia el backend.
- La identidad visual (colores, tipografía, modo claro/oscuro) se copia de `web/src/app/globals.css`.
  Si la duplicación empieza a molestar, se extrae a un paquete compartido.

## Alternativas consideradas

**Sección `/sistema` dentro de la app de Next.js de la tienda.** Un solo proyecto y deploy, pero mezcla código público
con privado (un error de configuración expone pantallas internas), acopla los deploys y contradice el ADR-003. Descartada.

**Segunda app de Next.js.** Mantiene la separación, pero no aprovecha nada de Next.js y agrega un servidor intermedio
por el que tiene que pasar la cookie de sesión de Spring: más piezas que pueden fallar sin ningún beneficio. Descartada.

## Consecuencias

**A favor:**

- Se usa cada herramienta para lo que sirve: Next.js para un sitio público, una SPA para una aplicación privada.
- Encaja con la seguridad ya implementada (sesión + CSRF para clientes de navegador), sin capas intermedias.
- Publicar el sistema es copiar archivos estáticos: no hay servidor de frontend que se pueda caer.
- Separación total entre el código de la tienda y el del sistema.
- Es el mismo React: componentes, hooks y TypeScript se escriben igual en las dos apps.

**En contra (costos que aceptamos):**

- Una herramienta más para aprender y mantener (Vite y un enrutador para el navegador).
- Los estilos de la marca están en dos lugares hasta que se extraigan a un paquete compartido.
- Sin HTML generado en el servidor, la primera carga muestra un instante de "cargando" (aceptable en una herramienta interna).
