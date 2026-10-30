# ADR-014: Stock con reservas y ficha de cliente vinculada a la tienda

- **Estado:** Aceptada
- **Fecha:** 2026-10-30

## Contexto

El sistema no tenía stock ni pantalla de clientes. El dueño decidió:

- El stock **se descuenta al confirmar el pago**, no al hacer el pedido.
- Si no hay stock **se muestra y se puede comprar igual** (queda reservado).
- Las reservas van **aparte**: el stock nunca baja de 0, y cuando llega mercadería el taller elige a qué pedido se la da.
- Los productos ya cargados arrancan **sin control** hasta que se les cargue una cantidad.
- Clientes: **una ficha por persona**, la misma de reparaciones, vinculada a la cuenta con la que compra en la tienda.

## Decisión

### 1. Módulo `stock`, nuevo

Tres tablas (V12): `existencias` (una fila por producto **con** control; sin fila = se vende sin límite),
`movimientos_stock` (historial: solo se insertan filas) y `reservas_stock`.

| Movimiento | Cuándo |
| --- | --- |
| INICIAL | se activa el control con lo que hay |
| INGRESO | llegó mercadería (con nota: proveedor, factura) |
| AJUSTE | se contó y había otra cantidad (motivo obligatorio) |
| VENTA | se confirmó el pago de un pedido |
| RESERVA_CUBIERTA | se separaron unidades nuevas para una reserva |
| DEVOLUCION | se canceló un pedido pagado |

**Confirmar el pago** descuenta lo que hay; lo que falta queda como reserva PENDIENTE.
**Cubrir una reserva** necesita todas sus unidades. **Cancelar un pedido pagado** devuelve lo vendido y lo cubierto,
y cancela lo pendiente. Descontar y devolver pasan **una sola vez** por pedido aunque se llamen dos veces.

Cargar y ajustar: Dueño y Mostrador. Dejar de controlar: solo el Dueño y sin reservas pendientes. El Técnico solo mira.

### 2. Dependencias: pedidos → stock, nunca al revés

Pedidos llama a `Stock` **dentro de su transacción**: o se confirma el pago y se descuenta, o ninguna de las dos.
Por eso no es un evento (que corre después, en otro hilo). La tabla de movimientos guarda el id del pedido **sin**
FOREIGN KEY: stock no conoce pedidos.

Cada línea de pedido guarda **qué productos lleva una unidad** (`lineas_pedido.productos`): un producto, los
componentes de una PC, o ninguno (el armado). Los pedidos viejos quedan vacíos y no descuentan nada.

### 3. Concurrencia

La existencia se lee con `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`): dos confirmaciones del último
producto se ponen en fila. Se bloquean siempre en el mismo orden (por id) para no trabarse entre sí.

### 4. La tienda

`GET /api/publico/stock` → `{slug, estado, quedan}` de los productos con control: **En stock**, **Últimas N** (hasta 3),
o **Sin stock · se reserva** (el botón dice "Reservar"). No se publica cuánto hay salvo en las últimas unidades.
El checkout y "Mis pedidos" marcan las líneas reservadas. El aviso se calcula sumando todo el pedido
(dos PCs con el mismo procesador piden 2).

### 5. Ficha de cliente vinculada

`clientes` gana `notas` (internas) y `cuenta_tienda_id` (única por taller). Con el **primer pedido** la cuenta se vincula sola:
1. si ya tiene ficha, esa;
2. si hay **una sola** ficha sin cuenta con el mismo correo, o si no con el mismo teléfono, esa;
3. si no, una ficha nueva con los datos de la cuenta.

Ante dos coincidencias no adivina (mejor una ficha de más que pegarle la cuenta a otra persona). Desde la ficha se
puede desvincular y vincular a mano por correo. Con la cuenta vinculada, el cliente ve **sus reparaciones** en
"Mis pedidos" (lo mismo que el seguimiento: nada interno).

`usuarios` expone `CuentasDeTienda` (solo lectura) para que `clientes` busque la cuenta. Dependencias:
pedidos → clientes → usuarios; reparaciones → clientes. Sin ciclos.

## Alternativas consideradas

- **Stock negativo como reserva:** más simple, pero mezcla "lo que hay" con "lo que se debe" y el taller no elige a quién atender primero.
- **Descontar al hacer el pedido:** bloquea stock con pedidos que nunca se pagan.
- **Evento `PagoConfirmado`:** desacopla, pero el descuento quedaría fuera de la transacción del pago.
- **Vincular por nombre:** hay homónimos; correo y teléfono son más confiables.

## Consecuencias

**A favor:** el stock se puede explicar leyendo el historial; nunca hay números negativos; la ficha junta reparaciones y compras.

**En contra:** dos clientes pueden comprar la última unidad a la vez (el segundo queda reservado al confirmar el pago);
fichas duplicadas de antes se unifican a mano (todavía no hay "fusionar fichas").
