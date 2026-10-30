# ADR-008: Catálogo en una sola tabla, precios en pesos enteros e historial de cambios

- **Estado:** Aceptada (la carga de productos, fotos y ofertas se suma en el [ADR-013](0013-catalogo-editable-fotos-y-promociones.md))
- **Fecha:** 2026-10-29

## Contexto

El módulo `catalogo` tiene que servir a tres cosas a la vez:

- La **tienda**: productos por categoría, buscador, página de cada producto.
- **Armá tu PC**: componentes con los datos técnicos de la compatibilidad básica (socket, tipo de memoria, formato, consumo, potencia).
- El **sistema de gestión**: una página para actualizar precios, uno por uno o con un aumento por porcentaje (en Argentina los precios cambian seguido).

## Decisión

### 1. Una sola tabla `productos`

Los componentes de Armá tu PC son productos con algunas columnas técnicas más (vacías en el resto).
Una sola lista de precios y un solo stock (a futuro); un componente también se puede publicar como producto suelto.

### 2. Precios en pesos enteros (`BIGINT`), sin centavos

El negocio no usa centavos. En Java, un value object `Pesos(long)` que no admite negativos ni valores absurdos.
Nunca `double`. Los aumentos se calculan con `BigDecimal` y se **redondean al peso** (las mitades, para arriba).

### 3. Historial de cada cambio de precio

Tabla `historial_precios`, de solo agregar (entidad `@Immutable`): precio anterior, nuevo, motivo (`MANUAL` o `PORCENTAJE`),
porcentaje, quién y cuándo. Se escribe **en la misma transacción** que el cambio: no puede quedar un precio cambiado sin su registro.
Guarda el nombre de quien lo cambió tal como era en ese momento, así se lee sin consultar el módulo de usuarios.

### 4. Aumentos con vista previa

`POST /api/sistema/precios/aumento/vista-previa` calcula sin cambiar nada; `POST .../aumento` aplica.
Un aumento a N productos es una sola transacción: se aplica todo o nada. El porcentaje va de -50% a +100%,
para frenar errores de tipeo que afectarían a todo el catálogo.

### 5. Permisos

Ver precios: todo el personal. Cambiarlos: solo el Dueño (regla del servicio, responde 403).

### 6. Filtros de la tienda en la aplicación, no en SQL

La base trae todos los productos del taller y el servicio filtra (categoría, condición, búsqueda sin tildes).
Un taller tiene cientos de productos: así las reglas quedan en un solo lugar y se testean sin base de datos.

## Alternativas consideradas

- **Dos tablas (productos y componentes):** columnas más limpias, pero dos listas de precios y dos de todo. Descartada.
- **Datos técnicos en una columna JSON:** flexible, pero la base no valida nada y las consultas son más difíciles. Descartada.
- **Precios con centavos (`NUMERIC(12,2)`):** el estándar para dinero, pero el negocio no los usa. Descartada; migrar a futuro es acotado porque todo pasa por `Pesos`.
- **Historial por evento asíncrono (Spring Modulith):** desacopla, pero si algo falla entre el cambio y el evento queda un precio sin registro. Descartada.
- **Filtros en SQL:** necesarios con decenas de miles de productos. Se revisa si algún taller llega a eso.

## Consecuencias

**A favor:** una sola fuente de precios; trazabilidad completa; aumentos masivos seguros y reversibles a mano mirando el historial;
el JSON público tiene la misma forma que los tipos de la web, así conectarla no cambia ninguna pantalla.

**En contra:** columnas técnicas vacías en la mayoría de los productos; si un día hacen falta centavos, hay que migrar la columna y `Pesos`.

## Datos de ejemplo

`db/ejemplos/R__catalogo_de_ejemplo.sql` carga el mismo catálogo que usa la web (con **precios inventados**),
solo con el perfil `dev`. Nunca en producción ni en los tests.
