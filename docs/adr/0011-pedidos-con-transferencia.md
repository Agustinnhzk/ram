# ADR-011: Pedidos con pago por transferencia y comprobante

- **Estado:** Aceptada
- **Fecha:** 2026-10-29

## Contexto

La tienda tiene carrito (en el navegador) y hasta ahora el pedido se mandaba por WhatsApp. El dueño quiere que el cliente
**pague por transferencia al alias del negocio** y **suba el comprobante en la página**, y verlo después en el sistema.
El pago online (Mercado Pago, Nave) queda para más adelante.

## Decisión

### 1. El cliente tiene que tener cuenta

Para ver sus pedidos y subir el comprobante después ("ya transferí"), el pedido tiene que ser de alguien.
El carrito sigue funcionando sin cuenta: la cuenta se pide al confirmar.

### 2. El precio lo pone el servidor

Del carrito solo viaja **qué** quiere el cliente (slug y cantidad; para una PC, el slug de cada componente).
El servidor busca cada cosa en el catálogo y usa el precio **de ese momento**. Lo que no existe, no está publicado o
no tiene precio ("a consultar") no se puede comprar.

Una PC de Armá tu PC se vuelve a revisar con las **mismas reglas** que la tienda (socket, tipo de memoria, formato,
fuente con margen, gráficos integrados): el navegador guía, el servidor decide.

Cada línea guarda descripción, cantidad y precio unitario **copiados**: si mañana cambia el precio, el pedido sigue valiendo lo mismo.

### 3. Estados

```
ESPERANDO_PAGO ──(sube comprobante)──▶ PAGO_INFORMADO ──(el taller confirma)──▶ PAGADO ──(entrega)──▶ ENTREGADO
      ▲                                       │
      └──────(el taller rechaza, con motivo)──┘          CANCELADO: antes de entregar
```

| Acción | Quién |
| --- | --- |
| Crear, subir comprobante | El cliente (solo los suyos) |
| Cancelar | El cliente mientras espera el pago; después, el taller |
| Confirmar pago, rechazar comprobante, cancelar | Dueño y Mostrador (es plata) |
| Entregar, ver | Todo el personal |

El taller también puede confirmar desde ESPERANDO_PAGO (pagó en efectivo al retirar).

### 4. Comprobante en la base (BYTEA)

JPG, PNG o PDF, hasta 5 MB, tipo verificado por sus primeros bytes (módulo `archivos`, ADR-010).
Los datos del comprobante (`comprobantes_pago`) y el archivo (`archivos_comprobantes`) van en **tablas separadas**:
abrir o listar pedidos no carga los archivos. Se guardan todos los que suba (máximo 5 por pedido); los rechazados quedan con el motivo.

Al abrirlo desde el sistema se manda con el tipo detectado, `inline`, sin caché y con `nosniff`.

### 5. Datos para transferir, por taller

Tabla `datos_de_pago` (alias, titular, entidad, CBU opcional). Los de RAM se cargan en la migración V8:
alias `ram.nh`, Agustin Negri Hrytezuk, Naranja X.

### 6. Avisos por mail con eventos

Al crear un pedido y al subir un comprobante se publican los eventos `PedidoRealizado` y `PagoInformado`.
Un listener (`@ApplicationModuleListener`) manda el mail al taller **después** de guardar, en otro hilo: el cliente no espera,
y Spring Modulith anota el evento en la misma transacción (tabla `event_publication`), así un mail fallido no se pierde.

### 6b. Armado de PC y envíos (V9)

- **Armado opcional:** al final de Armá tu PC el cliente elige "armada y probada" (+ el precio del taller, hoy $30.000) o
  "solo los componentes". En el pedido son dos líneas: los componentes y "Armado y prueba de la PC", con el precio de
  `ajustes_tienda.precio_armado_pc` (editable desde Precios en el sistema, ADR-013; NULL = el taller no ofrece armado). Desde el ADR-013, todos los pasos de Armá tu PC se pueden saltear: alcanza con una pieza.
- **Entrega:** `RETIRO`, `ENVIO_ROSARIO` (gratis) o `ENVIO_OTRA_LOCALIDAD`: el envío se cotiza por WhatsApp y se paga
  aparte (no entra en el total). Con envío, la dirección es obligatoria.
- **Cotización:** `POST /api/tienda/pedidos/cotizacion` calcula el pedido sin guardarlo: el checkout muestra los precios
  del servidor antes de confirmar.

### 7. Límites

5 pedidos esperando pago por cliente; 30 productos distintos y 20 unidades por línea; 5 comprobantes por pedido.

### 8. Número de pedido

Correlativo por taller (último + 1), único en la base. Si dos pedidos toman el mismo número a la vez, el segundo
recibe 409 "probá de nuevo". Con el tráfico de un taller es rarísimo.

## Alternativas consideradas

- **Comprobante en disco o en un bucket (S3, R2):** escala mejor, pero es otro servicio que configurar, cuidar y respaldar. Con pocos pedidos por día, la base alcanza y entra en los backups. Si crece, se mueve solo la tabla `archivos_comprobantes`.
- **Comprobante por mail:** no queda asociado al pedido y se pierde entre otros mails.
- **Pedido sin cuenta (solo con mail y teléfono):** menos fricción, pero el cliente no puede volver a su pedido para subir el comprobante.
- **Precio enviado desde el carrito:** imposible: cualquiera lo edita desde la consola del navegador.

## Consecuencias

**A favor:** el pedido no se puede manipular; historial completo de pagos y rechazos; el taller recibe aviso por mail.

**En contra:** las reglas de Armá tu PC están en dos lenguajes (TypeScript y Java): si cambia una, cambian las dos (los tests de cada lado lo recuerdan).
La base crece con los archivos (5 MB como mucho cada uno).
El stock se descuenta al confirmar el pago, en la misma transacción (ADR-014).
