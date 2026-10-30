# ADR-013: Catálogo editable desde el sistema, fotos en la base, ofertas, banners y Armá tu PC sin pasos obligatorios

- **Estado:** Aceptada
- **Fecha:** 2026-10-29

## Contexto

Hasta acá, el catálogo se cargaba con la base (datos de ejemplo) y desde el sistema solo se cambiaban precios.
La tienda usaba sus propios datos de ejemplo. El dueño necesita cargar productos y componentes con fotos,
poner ofertas, cambiar los flyers de la portada y el precio del armado, y que todo se vea en la página.

## Decisión

### 1. La tienda lee el catálogo del backend

`web/src/lib/api` pide categorías, productos, componentes, banners y precio del armado a `/api/publico/...`.
Lo hace el servidor de Next, con `X-Forwarded-Host` = el dominio del taller (`TALLER_DOMINIO`, por defecto
`ram.localhost`). Caché de 30 segundos: un cambio en el sistema se ve en la tienda en menos de un minuto.
Para compilar la tienda (`npm run build`) el backend tiene que estar prendido.

### 2. Alta y edición de productos, solo el Dueño

Ficha (nombre, categoría, condición, resumen, descripción, características, garantía, publicado) y, si es un
componente de Armá tu PC, sus datos técnicos (validados por tipo; los que no corresponden se descartan).
El **slug se genera del nombre y no cambia nunca** (los links viejos siguen andando). El precio inicial queda
en el historial; después se cambia por la página de precios. Categorías: crear y renombrar (el slug tampoco cambia).
No se borran productos: se ocultan (publicado = no), porque hay historial y pedidos que los nombran.

### 3. Fotos en la base, ya preparadas

Hasta 6 por producto, JPG o PNG, hasta 10 MB. Al subirlas, el backend (solo con Java, ImageIO):
**las endereza** según el EXIF (las del celular vienen "acostadas"), **las achica** (1600 px y 600 px) y
**las guarda como JPEG** en `archivos_fotos` (los datos van aparte, en `fotos_productos`, como los comprobantes).
Se sirven en `/api/publico/fotos/{id}` con caché de un año ("immutable": una foto nueva es otro id).
Se rechazan imágenes de más de 50 megapíxeles (una imagen chica en bytes puede ser enorme al abrirla).

No se genera WebP: Java no lo escribe sin librerías nativas. Las fotos ya llegan livianas.

### 4. Ofertas: precio y fecha de fin

`precio_oferta` + `oferta_hasta` (inclusive, fecha de Argentina). El **precio vigente** es el de oferta si no venció
y sigue siendo menor al de lista; si no, el de lista. La tienda muestra el de lista tachado; **los pedidos cobran
el vigente**. Los aumentos por porcentaje cambian el precio de lista, no la oferta.

### 5. Banners (módulo `vidriera`)

Los flyers del carrusel: imagen (achicada a 1200 px), texto alternativo obligatorio, link, activo y fechas
opcionales, orden. **El link solo puede ser una página de la tienda** (`/...`): nada de `javascript:` ni otros sitios.
Los 6 flyers que ya tenía RAM se cargan en la migración V11, apuntando a las imágenes de la web.

### 6. Armá tu PC: todo se puede saltear

Cada paso tiene "Saltear". La compatibilidad se revisa solo entre las piezas elegidas (sin procesador, se ven
todas las mothers). Hace falta al menos una pieza. Sin placa de video y con un procesador sin gráficos integrados,
la tienda avisa pero deja comprar. Las mismas reglas en la tienda (`arma-tu-pc.ts`) y en el backend (`ArmadoDePc`).

### 7. Precio del armado editable

En Precios. Vacío = el taller deja de ofrecer el armado (se venden solo los componentes).

## Alternativas consideradas

- **Fotos en un bucket (S3/R2):** mejor a gran escala; descartada por ahora (otro servicio, otras credenciales). Se migra tocando solo `FotosDeProductosJpa`.
- **Optimizador de imágenes de Next:** las pediría sin el dominio del taller; además ya llegan achicadas. Se usa `unoptimized` para las de `/api`.
- **Borrar productos:** rompería el historial y los pedidos. Se ocultan.

## Consecuencias

**A favor:** el dueño maneja el catálogo solo; las fotos del celular se ven bien; la tienda nunca cobra un precio
que no es el vigente.

**En contra:** la base crece con las fotos (~300 KB por foto entre los dos tamaños); compilar la tienda requiere el backend.
