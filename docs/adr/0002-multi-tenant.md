# ADR-002: Multi-tenant con `tenant_id`, taller identificado por dominio

- **Estado:** Aceptada
- **Fecha:** 2026-10-27

## Contexto

RAM arranca con un solo taller, pero la idea es venderlo como SaaS a otros talleres. Cada taller (tenant) tiene su tienda, sus clientes, su stock y sus órdenes, y nunca puede ver los datos de otro.

Hay dos tipos de acceso con necesidades distintas:

- **La tienda pública** la visita cualquiera sin loguearse: ver productos y servicios no requiere cuenta. Loguearse solo hace falta para comprar. Entonces el taller no se puede deducir del usuario.
- **El sistema de gestión** lo usa solo el personal del taller, siempre logueado.

Agregar el soporte multi-tenant después de construir el sistema implicaría tocar todas las tablas y consultas. Por eso se decide ahora, aunque haya un solo taller.

## Decisión

### 1. Datos: una base compartida con `tenant_id`

Todas las tablas del negocio llevan una columna `tenant_id` desde la primera migración. RAM es el primer tenant.

Doble protección contra fugas entre talleres:

- **Filtro automático en la capa de persistencia**: toda consulta se filtra por el tenant actual sin que el código del módulo lo escriba a mano.
- **Row-Level Security en PostgreSQL**: aunque una consulta se olvide el filtro, la base de datos no devuelve filas de otro taller.

### 2. Identificación del taller: por dominio

El backend mira el dominio de cada request y busca a qué taller pertenece.

- Cada taller tiene un **subdominio** de la plataforma (`taller.plataforma.com.ar`).
- Opcionalmente, un **dominio propio** (`ramservicios.com.ar`) apuntando al mismo taller.
- En el sistema de gestión, además, el taller del usuario logueado tiene que coincidir con el del dominio; si no coincide, se rechaza la request.

### 3. Clientes: una cuenta por taller

Un cliente se registra en la tienda de un taller. Si compra en dos talleres, son dos cuentas separadas. El email es único dentro de cada taller, no en toda la plataforma.

### 4. Personal: un empleado pertenece a un solo taller

Cada empleado tiene usuario en un solo taller, con su rol. El dueño de la plataforma tiene un rol aparte (súper administrador) que no pertenece a ningún taller.

## Alternativas consideradas

**Un schema o una base de datos por taller.** Mejor aislamiento, pero cada migración se corre N veces y operar muchos talleres se vuelve pesado y caro. Se descartó para esta etapa; con `tenant_id` + RLS el aislamiento es suficiente.

**Identificar el taller por ruta** (`plataforma.com.ar/taller`). Más simple, pero se ve poco profesional y complica el SEO de cada taller. Descartada.

**Identificar el taller solo por el usuario logueado.** No sirve para la tienda pública, que se navega sin cuenta. Descartada.

**Cuenta de cliente global** (una cuenta para todos los talleres). Más cómoda para el cliente, pero mezcla datos entre talleres y obliga a decidir quién es "dueño" del cliente. Para un taller, su cartera de clientes es su negocio. Descartada.

**Empleado en varios talleres.** Útil para cadenas, pero obliga a elegir "en qué taller estoy trabajando" al entrar. Se deja para cuando aparezca el caso; el modelo permite pasar a eso sin romper nada.

## Consecuencias

**A favor:**

- Sumar un taller nuevo es insertar una fila, no crear infraestructura.
- Una sola base y una sola migración para todos.
- Cada taller es dueño de sus clientes, lo que es un argumento de venta del SaaS.

**En contra (costos que aceptamos):**

- Un error en el filtro podría mostrar datos de otro taller. Se mitiga con RLS y con un **test de integración obligatorio** que verifica que un taller no puede leer datos de otro.
- Los dominios propios requieren gestionar DNS y certificados HTTPS por taller.
- Un taller muy grande comparte recursos con los demás; si pasara, se podría mover a su propia base más adelante.
