# ADR-018: Trabajos realizados publicados en la tienda

- **Estado:** Aceptada
- **Fecha:** 2026-11-17

## Contexto

Para conseguir clientes por Google en Rosario y alrededores conviene mostrar trabajos reales con el barrio
donde se hicieron ("Notebook que no encendía — Abasto", "4 cámaras en una casa de Funes"). Es contenido propio,
que nadie puede copiar, y le muestra a Google y a la gente que trabajamos en esas zonas.

## Decisión

- **Módulo nuevo `trabajos`**, con pantalla propia en el sistema. Se cargan a mano, no salen solos de las
  órdenes: así sirve también para trabajos viejos o sin orden. El número de orden se puede anotar, pero solo para
  el taller (no se publica y no se valida contra el módulo de reparaciones: sin dependencia entre módulos).
- **Barrio escrito libre**, con sugerencias de los ya usados. Se guarda como se muestra ("Alberdi") y
  normalizado ("alberdi") para agrupar: "B° Alberdi", "barrio alberdi" y "Alberdi" son la misma zona.
  No acepta algo que parezca una dirección (un número de 3 o más cifras).
- **Privacidad del cliente:**
  - solo se publica si se marca que el cliente dio permiso (lo controla el dominio y un CHECK en la base);
  - nunca calle ni altura;
  - las fotos se vuelven a guardar como JPEG sin EXIF, así no queda la ubicación GPS del celular;
  - las fotos de un trabajo sin publicar no se ven desde la tienda.
- **URL estable:** `/trabajos/{slug}`, con el slug del título y el barrio. No cambia al editar.
- **Fotos** como las de productos: dos tamaños (1600 y 600 px), hasta 8, bajo `/api/publico/fotos/trabajos/`
  para que robots.txt deje que Google las vea.
- Cargar, editar, publicar y borrar: solo el Dueño (igual que el catálogo).
- **Video opcional (V16):** solo el link de un reel de Instagram o de un video de YouTube. El video lo alojan
  ellos (no se sube al servidor: pesaría mucho en la base y en el ancho de banda). El link se guarda limpio y la
  tienda carga el reproductor recién cuando se toca "play".
- **Menú:** "Trabajos" aparece en el menú de arriba recién cuando hay al menos uno publicado.

## Alternativas consideradas

- **"Publicar como caso" al cerrar una orden:** más automático, pero ata trabajos a reparaciones e instalaciones
  y no sirve para trabajos sin orden. Se puede agregar después como atajo que abra este mismo formulario.
- **Lista fija de barrios:** evita duplicados, pero obliga a cargar la lista antes. La normalización y las
  sugerencias resuelven los duplicados sin eso.
- **Páginas por zona (`/zonas/funes`)** desde el primer día: sin varios casos por zona serían páginas vacías,
  que Google trata como relleno. Quedan para cuando haya casos.

## Consecuencias

**A favor:** contenido local real para Google, confianza para quien llega, y la privacidad del cliente cuidada
por diseño.

**En contra:** depende de que se carguen casos seguido; sin casos, `/trabajos` queda vacía (la tienda la oculta
del menú y del sitemap mientras no haya ninguno publicado).
