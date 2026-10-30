# ADR-016: Borrar clientes y pedidos, y límite de intentos de ingreso

- **Estado:** Aceptada
- **Fecha:** 2026-10-30

## Contexto

Antes de abrir la tienda, el dueño va a hacer compras de prueba en línea y quiere poder borrarlas. Además, con la
tienda y el sistema en internet, cualquiera puede probar contraseñas en los formularios de ingreso.

## Decisión

### 1. Borrar, solo el Dueño y con confirmación

- **Pedido:** se borra con sus líneas, comprobantes y archivos. Si estaba **pagado**, antes devuelve el stock
  (igual que al cancelarlo). Los movimientos de stock quedan en el historial.
- **Cliente:** se borra la ficha y, si tiene, **su cuenta de la tienda** (el mail queda libre para registrarse de nuevo,
  y sus sesiones se cierran). Si tiene pedidos, reparaciones, instalaciones o abonos, **la base no deja**
  (FOREIGN KEY) y el sistema explica qué borrar primero. Así no hay que consultar otros módulos (no hay ciclos)
  y es imposible dejar un pedido o una reparación sin cliente.
- Todo en una transacción: si algo falla, no se borra nada.

Para un pedido real que no sigue está **cancelar**, que deja registro. Borrar es para limpiar pruebas.

### 2. Límite de intentos de ingreso (sistema y tienda)

En los últimos 15 minutos: **5 fallos por correo** o **20 por IP** → 429 "Hiciste muchos intentos". Ingresar bien
borra los fallos de ese correo. Por correo protege la cuenta aunque cambien de IP; por IP frena a quien prueba muchos
correos. En memoria, como los otros limitadores (ADR-009, ADR-012).

## Alternativas consideradas

- **"Borrar datos de prueba" de una vez:** más seguro a largo plazo, pero el dueño prefiere borrar uno por uno.
- **Borrado lógico (marcar como borrado):** útil para auditoría, pero lo que se quiere es que no quede nada.
- **Captcha en el login:** suma un servicio externo; se agrega si el límite no alcanza.

## Consecuencias

**A favor:** se prueba en línea y se limpia; adivinar contraseñas pasa a ser impracticable.

**En contra:** un clic del Dueño (con confirmación) borra un pedido sin vuelta atrás. El límite vive en memoria:
reiniciar el backend lo resetea, y con varios servidores habría que pasarlo a la base o a Redis.
