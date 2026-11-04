# ADR-017: Deploy en un solo servidor con Docker Compose y Caddy

- **Estado:** Aceptada
- **Fecha:** 2026-11-03

## Contexto

RAM tiene que salir a internet: tienda, sistema, backend y base. El presupuesto es el de un negocio chico, así que
va a un Cloud Server de DonWeb (en Argentina, se paga en pesos). El hosting web común no sirve: solo corre PHP.

El diseño ya contaba con esto: el backend elige el taller por el dominio (ADR-002), el sistema vive en `sistema.` +
el dominio de la tienda (ADR-003) y la tienda y el sistema llaman a `/api` en su propio dominio, sin CORS (ADR-007).

## Decisión

Todo en un servidor, con **Docker Compose** (`deploy/compose.yaml`): PostgreSQL, backend, tienda y **Caddy** adelante.

- **Caddy** y no Nginx + certbot: saca y renueva los certificados HTTPS solo, con una configuración de 30 líneas.
  Reparte por dominio: la tienda a Next, el sistema a sus archivos estáticos, y `/api` de los dos al backend.
- **Solo Caddy da a internet** (80 y 443). El backend y la base se ven solo en la red interna de Docker. Spring confía
  en los encabezados `X-Forwarded-*` de Caddy porque vienen de esa red (`forward-headers-strategy: native`).
- **El sistema se compila dentro de la imagen de Caddy**: son archivos estáticos, no necesitan un contenedor propio.
- **La tienda se compila al arrancar**, no al armar la imagen: Next arma las páginas del catálogo pidiéndole los
  datos al backend, que recién existe cuando arranca el compose. Un script espera a que el backend responda.
- **Las claves no van en git ni en las imágenes**: `deploy/.env` (dominio y clave de la base) y
  `deploy/application.yml` (dueño inicial y mail, mismo formato que el de desarrollo), montado en el backend.
- **El dominio propio del taller se carga en la base** (`talleres_dominios`), no en una migración: es un dato del
  taller, como lo será para cada taller nuevo.
- Actualizar es `git pull` + `docker compose up -d --build` (`deploy/actualizar.sh`). Backup diario con `pg_dump`
  (`deploy/backup.sh`).

## Consecuencias

- Un solo servidor que mantener; si se cae, se cae todo. Para un taller alcanza; con muchos talleres se puede separar
  la base o sumar servidores sin cambiar el código.
- Cada arranque de la tienda tarda uno o dos minutos (compila). Mientras, Caddy responde 502.
- Los backups quedan en el mismo servidor: hay que bajarlos a otro lado cada tanto.
- Los Dockerfiles corren `mvnw` y scripts de Linux: `.gitattributes` fuerza el fin de línea de Linux aunque se
  edite en Windows.
