# ADR-012: "Olvidé mi contraseña" con link por mail

- **Estado:** Aceptada
- **Fecha:** 2026-10-29

## Contexto

Los clientes de la tienda se registran con correo y contraseña (ADR-006). Si se olvidan la contraseña, hoy tienen que escribirle
al taller. Ya tenemos envío de mails (ADR-010), así que pueden recuperarla solos.

## Decisión

1. **Pedir el link:** `POST /api/tienda/recuperar-clave {correo}`. La respuesta es **siempre la misma** (202), haya o no una cuenta
   con ese correo: así nadie puede averiguar quién es cliente. El mail sale en otro hilo, para que tampoco se note midiendo el tiempo.
2. **El token:** 32 bytes al azar (256 bits). En la base se guarda solo su **SHA-256** (tabla `recuperaciones_de_clave`):
   quien lea la base o un backup no puede usar los links. Vale **1 hora** y **una sola vez**; pedir otro anula el anterior.
3. **El link:** `https://<tienda>/recuperar-clave/nueva#<token>`. El token va después de `#`, la parte de la dirección que el
   navegador **no manda al servidor**: no queda en logs ni en el encabezado Referer. La página lo lee y lo borra de la barra.
   La dirección de la tienda sale del pedido, que ya pasó por el filtro de talleres (solo dominios conocidos).
4. **Elegir la nueva:** `POST /api/tienda/restablecer-clave {token, clave}`. Mismas reglas de contraseña que el registro.
   Al cambiarla se **cierran todas las sesiones** de esa cuenta (Spring Session las encuentra por el id del usuario).
5. **Límites:** 3 pedidos por correo y 10 por IP cada hora (en memoria, como ADR-009 y ADR-010).

## Alternativas consideradas

- **Mandar una contraseña nueva por mail:** queda escrita en el mail para siempre. Descartada.
- **Token en la query (`?token=`):** queda en logs del servidor y del proxy. Descartada por el `#`.
- **Guardar el token con BCrypt:** innecesario: un token de 256 bits al azar no se puede adivinar probando; SHA-256 alcanza.
- **El mail con el registro de eventos de Spring Modulith:** guardaría el link en `event_publication`. Descartada.

## Consecuencias

**A favor:** el cliente se arregla solo; ni la base ni los logs exponen links válidos; no se filtra quién tiene cuenta.

**En contra:** si el mail no sale, el link se pierde (el cliente pide otro). El personal del sistema no tiene recuperación
todavía: el Dueño les cambia la clave (pendiente para la pantalla de usuarios).
