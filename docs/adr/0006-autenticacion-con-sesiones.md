# ADR-006: Autenticación propia con sesiones en el servidor

- **Estado:** Aceptada
- **Fecha:** 2026-10-28

## Contexto

Hasta ahora `/api/sistema` estaba abierto. Antes de construir reparaciones hace falta saber quién hace cada cosa y que nadie de afuera entre al sistema.

Hay dos tipos de usuario que nunca se mezclan (ADR-003):

- **Personal del taller**: entra al sistema de gestión. En RAM es una sola persona, pero los talleres del SaaS pueden tener empleados con distintos permisos.
- **Clientes de la tienda**: se registran para comprar y para ver sus reparaciones.

Y todo es por taller (ADR-002): una cuenta pertenece a un taller y solo sirve en él.

## Decisión

### Login propio con Spring Security

- Las contraseñas se guardan con **BCrypt** (con el prefijo `{bcrypt}`, para poder cambiar de algoritmo sin invalidar las existentes). Máximo 72 bytes, porque BCrypt ignora el resto.
- El login lo resuelve nuestro caso de uso (`ServicioDeAcceso`), no el `UserDetailsService` de Spring: así distingue personal de clientes y respeta el taller.
- Un error de login es siempre `credenciales_invalidas`, exista o no el correo, y tarda lo mismo en ambos casos: no se puede averiguar quién tiene cuenta.

### Sesión en el servidor, guardada en PostgreSQL

- Al loguearse, el servidor crea una sesión y el navegador recibe una cookie `SESSION` (HttpOnly: el JavaScript no la puede leer).
- Las sesiones se guardan en PostgreSQL con **Spring Session JDBC**: sobreviven a un reinicio de la aplicación y cerrar sesión las borra al instante.
- Se cierran tras 4 horas sin actividad.
- Al loguearse se cambia el id de sesión, contra ataques de fijación de sesión.
- **CSRF**: todo pedido que modifica algo debe traer el token de la cookie `XSRF-TOKEN` en el header `X-XSRF-TOKEN`.

### Roles

| Rol | Uso |
| --- | --- |
| Dueño | Todo. Es el único que usa RAM |
| Técnico | Diagnósticos y reparaciones; puede ver el password de los equipos |
| Mostrador | Ingresos, salidas, clientes y cobros; no ve passwords |

Los clientes tienen su propio permiso (`CLIENTE`) y nunca pasan las reglas de `/api/sistema`.

### Una sesión vale solo en su taller

La sesión guarda el taller donde se inició. Si aparece en otro taller, se rechaza y se cierra (`sesion_de_otro_taller`).

### El primer usuario

Al arrancar, si está configurado `ram.dueno-inicial` y el taller no tiene personal, se crea el dueño. Esa configuración va en `backend/config/application.yml`, fuera del repositorio.

## Alternativas consideradas

**Servicio externo de identidad** (Keycloak, Auth0, Clerk). Resuelve 2FA y login social, pero agrega una pieza que operar o un costo por usuario. Desproporcionado para esta etapa; el diseño permite sumar login con Google más adelante.

**Tokens JWT.** Muy usados, pero un token robado sirve hasta que vence y no se puede revocar fácil, y guardarlo en el navegador lo expone a scripts maliciosos. Con dos webs y un solo backend, la sesión en el servidor es más segura y más simple. Si aparece una app móvil, se reevalúa.

**Sesiones en memoria.** Más simple, pero se pierden en cada reinicio o deploy, y no escalan a más de una instancia.

## Consecuencias

**A favor:**

- Cerrar sesión o desactivar a alguien es inmediato.
- La cookie no es accesible desde JavaScript.
- Personal y clientes quedan separados por permisos, no por convención.

**En contra (costos que aceptamos):**

- Cada request lee la sesión de la base; para el volumen de un taller es despreciable.
- El frontend tiene que manejar el token CSRF.
- Pendiente: verificación de correo y recuperación de contraseña (necesitan el envío de mails), y límite de intentos de login.
