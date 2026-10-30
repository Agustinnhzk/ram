# ADR-009: Reparaciones — estados fijos, bitácora y seguimiento con verificación

- **Estado:** Aceptada
- **Fecha:** 2026-10-29

## Contexto

Las reparaciones son el corazón del sistema: un equipo entra, se diagnostica, se presupuesta, se repara (o no) y sale con garantía.
El cliente quiere saber en qué está su equipo sin llamar por teléfono. El dueño trabaja solo, pero el sistema
se va a vender a talleres con Técnico y Mostrador.

## Decisión

### 1. Estados fijos, con las transiciones válidas en el dominio

| Desde | Puede pasar a |
| --- | --- |
| INGRESADO | EN_DIAGNOSTICO |
| EN_DIAGNOSTICO | PRESUPUESTADO, SIN_REPARACION |
| PRESUPUESTADO | APROBADO, RECHAZADO |
| APROBADO | EN_REPARACION |
| EN_REPARACION | ESPERANDO_REPUESTO, EN_PROVEEDOR, LISTO_PARA_RETIRAR, SIN_REPARACION |
| ESPERANDO_REPUESTO | EN_REPARACION |
| EN_PROVEEDOR | EN_REPARACION, LISTO_PARA_RETIRAR |
| RECHAZADO, SIN_REPARACION | LISTO_PARA_RETIRAR (sin costo) |
| LISTO_PARA_RETIRAR | ENTREGADO |
| ENTREGADO | — (final) |

El enum `EstadoReparacion` sabe a qué estados puede pasar cada uno; un salto inválido (de INGRESADO a ENTREGADO) es un error.
Sumar un estado es tocar código; si al venderlo otros talleres necesitan flujos propios, se revisa.

### 2. El número de orden lo escribe el dueño

Como lo hace hoy (900, 901…). Único por taller: lo garantiza la base (`UNIQUE (taller_id, numero)`), y el sistema
muestra el último usado como ayuda.

### 3. Bitácora de movimientos

Cada cambio de estado, presupuesto, entrega o nota queda en `movimientos_reparacion` con fecha, quién y una nota opcional.
Los **cambios de estado los ve siempre el cliente** (son su línea de tiempo). Las **notas** pueden ser internas
o para el cliente: si un cambio de estado lleva una nota interna, se guardan dos movimientos (el cambio, visible, y la nota, interna).

### 4. Presupuesto: monto total + descripción

En pesos, sin centavos (como el catálogo, ADR-008). El cliente lo aprueba por WhatsApp y el taller lo registra.

### 5. Seguimiento: número de orden + últimos 4 dígitos del teléfono

`POST /api/publico/seguimiento {"numero": 901, "telefono": "7613"}`. El número solo es fácil de adivinar
(902, 903…), así que se pide un dato que solo tiene el cliente.

Como 4 dígitos son 10.000 combinaciones, hay **límite de intentos**: 5 fallidos por orden bloquean esa orden 15 minutos,
y hay un tope de 30 intentos por IP cada 15 minutos (429). Ante cualquier fallo la respuesta es la misma
("no encontramos una reparación con esos datos"): no revela si el número existe.

No usa sesión ni modifica nada, por eso no exige token CSRF.

El límite por IP necesita la IP real del cliente, que llega a través del proxy (Next en desarrollo, el servidor web
en producción) en `X-Forwarded-For`. Spring la acepta con `server.forward-headers-strategy: native`, que solo
confía en esos encabezados si vienen de un proxy de la red interna.

El seguimiento muestra estado, movimientos para el cliente, presupuesto y garantía. **Nunca** el password del equipo,
las observaciones ni las notas internas.

### 6. Password del equipo en texto, con acceso restringido (riesgo aceptado)

Se guarda como texto. Mitigaciones: solo lo reciben Dueño y Técnico (Mostrador y el seguimiento no), nunca aparece en
`toString()`, logs ni errores. **Riesgo aceptado:** quien acceda a la base o a un backup lo ve.
Para la versión SaaS se recomienda cifrarlo (AES-GCM con clave fuera de la base); el cambio queda acotado a la persistencia.

### 7. Comprobante: página imprimible + WhatsApp

El sistema muestra el comprobante listo para imprimir (o "Guardar como PDF") y arma el mensaje de WhatsApp con el
link de seguimiento. El PDF generado en el servidor queda para el módulo de mails.

### 8. "En garantía" se calcula

Al entregar se carga cuántos días de garantía tiene **esa** reparación (0 = sin garantía) y se guarda la fecha de vencimiento.
"En garantía" o "Finalizada" se calcula comparando esa fecha con hoy: no hay tareas programadas que puedan fallar.

## Consecuencias

**A favor:** el estado nunca queda inconsistente; historia completa de cada equipo; el cliente se informa solo;
el seguimiento no expone datos de otros clientes.

**En contra:** el password no está cifrado (ver 6); el límite de intentos vive en memoria del servidor
(si un día hay varias instancias, se pasa a la base o a Redis).
