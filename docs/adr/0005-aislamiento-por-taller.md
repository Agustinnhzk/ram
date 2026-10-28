# ADR-005: Aislamiento de datos por taller con `@TenantId` de Hibernate

- **Estado:** Aceptada
- **Fecha:** 2026-10-27
- **Relacionada con:** ADR-002 (la complementa y posterga una parte)

## Contexto

El ADR-002 decidió una base compartida con `tenant_id` y doble protección: un filtro automático en la persistencia y Row-Level Security (RLS) en PostgreSQL. Con el módulo `clientes` aparece la primera tabla con datos de un taller, y hay que implementarlo.

Al implementarlo apareció un problema con RLS: **PostgreSQL no aplica RLS a los superusuarios**, y el usuario que crean por defecto la imagen de Docker y Testcontainers es superusuario. Un test de RLS en este entorno pasaría aunque las políticas estuvieran mal. Hacerlo bien requiere dos usuarios de base de datos (uno dueño de las tablas para las migraciones, otro sin privilegios para la aplicación), que es configuración de infraestructura.

## Decisión

### Ahora: filtro automático de Hibernate

- Cada tabla con datos de un taller tiene una columna `taller_id`, mapeada con `@TenantId` en su entidad.
- Un resolvedor (`ResolvedorTallerHibernate`, en el módulo tenants) le indica a Hibernate el taller de la request.
- Con eso, Hibernate completa `taller_id` al guardar y agrega el filtro por taller a todas las consultas. El código de los módulos no menciona el taller.
- **Falla cerrado:** si no hay taller en el contexto, el resolvedor devuelve un id que no existe y las consultas no devuelven nada.
- Cada módulo con datos por taller suma un test de aislamiento contra PostgreSQL real (como `AislamientoEntreTalleresTests`).

### Después: Row-Level Security

Se posterga a la etapa de infraestructura y deploy, cuando se creen los usuarios de base de datos separados. Ahí se agregan las políticas y un test que corra con el usuario sin privilegios.

## Alternativas consideradas

**Filtrar a mano en cada consulta** (`where taller_id = ?`). Se descartó: alcanza con olvidarse una vez para mostrar datos de otro taller.

**Filtros de Hibernate con `@Filter`.** Hay que activarlos en cada sesión y es fácil que una consulta quede sin filtro. `@TenantId` está pensado justo para esto y además completa el taller al guardar.

**Implementar RLS ahora con el usuario superusuario.** Se descartó porque daría una falsa sensación de seguridad: las políticas no se aplicarían y los tests pasarían igual.

## Consecuencias

**A favor:**

- El aislamiento no depende de que cada desarrollador se acuerde de filtrar.
- Las entidades y consultas de los módulos quedan simples.
- Un test por módulo prueba que funciona contra la base real.

**En contra (costos que aceptamos):**

- Hasta que llegue RLS hay una sola capa de protección. SQL escrito a mano (fuera de Hibernate) no pasa por el filtro; por eso, por ahora, todo acceso a datos de un taller va por JPA.
- El aislamiento depende de que el contexto del taller se limpie al terminar cada request (lo hace el filtro web en un `finally`, y hay un test que lo verifica).
