# ADR-010: "Trabajá con nosotros" por mail, y un módulo de notificaciones

- **Estado:** Aceptada
- **Fecha:** 2026-10-29

## Contexto

La página "Trabajá con nosotros" de la tienda tiene dos públicos:

- **Personas** que quieren trabajar en el taller: mandan sus datos y su CV.
- **Empresas** que quieren tercerizar su soporte IT o hacer una alianza comercial con RAM.

Hoy el dueño atiende todo desde su mail. No hace falta una pantalla para gestionar postulaciones: alcanza con que lleguen bien armadas y se puedan responder.

Además, otros módulos van a necesitar mandar mails (avisos de pedidos, y más adelante comprobantes de reparación).

## Decisión

### 1. Módulo `notificaciones` con una sola puerta: `Correos`

`Correos.enviarAlTaller(mensaje)`: los demás módulos dicen **qué** mandar, no **cómo**. A dónde llega lo decide la configuración
(`ram.correo.destino-del-taller`; para pruebas, `agustinnegri6@gmail.com`).

- Con `spring.mail.host` configurado → se manda por SMTP (`spring-boot-starter-mail`).
- Sin servidor de correo (tests, tu máquina sin credenciales) → **se escribe en el log**. Nada falla y se ve qué se hubiera mandado.

La clave del correo va **solo** en `backend/config/application.yml` (fuera de git).

### 2. Contactos: se mandan, no se guardan

`POST /api/publico/contactos/postulacion` (multipart, con el CV) y `POST /api/publico/contactos/empresa` (JSON).
Arman un mail con "Responder a" apuntando a quien escribió: responder es tocar "Responder".

El mail se manda **en el momento**: si falla, la persona ve "no pudimos enviar tu mensaje" (503) y puede reintentar
o escribir por WhatsApp, en vez de creer que lo mandó.

### 3. Archivos: el tipo se decide por los primeros bytes

Módulo `archivos` (lo comparten contactos y pedidos): un archivo subido se acepta solo si su **firma** (primeros bytes) es de un
tipo permitido (CV: PDF, DOC, DOCX). El nombre y el Content-Type que manda el navegador se ignoran: son fáciles de falsificar.
Hasta 5 MB. El nombre se limpia (sin carpetas, tildes ni caracteres raros) antes de usarlo en un mail o una descarga.

### 4. Antispam

- **Límite por IP**: 5 envíos por hora (en memoria, como el seguimiento del ADR-009).
- **Campo trampa** (`sitioWeb`): escondido en el formulario. Si viene con algo, es un robot: se responde 202 sin mandar nada.
- Sin CSRF: no hay sesión de nadie que proteger.
- Saltos de línea fuera del asunto del mail (evita encabezados inyectados).

## Alternativas consideradas

- **Guardar postulaciones en la base y verlas en el sistema:** más trabajo y una pantalla más, para algo que hoy se resuelve en el mail. Se puede sumar después sin cambiar la tienda.
- **Captcha (reCAPTCHA, Turnstile):** frena más robots, pero suma un servicio externo y molesta a la persona. Se agrega si el campo trampa y el límite no alcanzan.
- **Mail "de fondo" (asíncrono) también para contactos:** la persona no esperaría, pero no se enteraría si falla.

## Consecuencias

**A favor:** cero pantallas nuevas en el sistema; los mails se responden directo; los tests no necesitan servidor de correo.

**En contra:** si el mail falla y la persona no reintenta, la postulación se pierde (no queda en la base).
El destino es uno solo para toda la plataforma: con más talleres, pasa a ser un dato de cada taller.

## Para producción

Gmail sirve para pruebas (con una "contraseña de aplicación" de Google). Para producción conviene un servicio de mails
transaccionales (Brevo, Resend, Amazon SES) con el dominio propio configurado (SPF/DKIM): llegan más a la bandeja de entrada
y no a spam. Es cambiar `spring.mail.*`, no código.
