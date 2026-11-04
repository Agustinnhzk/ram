# R.A.M. Seguridad y Tecnología

Este es el sistema de mi negocio, RAM (Reparación, Asesoramiento y Mantenimiento). En Rosario arreglo notebooks y PC, instalo cámaras y alarmas, hago monitoreo y vendo equipos. Necesitaba una web para vender y un sistema para manejar el taller, y en vez de usar algo armado lo hice desde cero. Además es mi proyecto de portfolio.

Son tres partes que comparten el mismo backend:

- **La tienda** (`web/`): la página pública. Catálogo, Armá tu PC, carrito, pago por transferencia subiendo el comprobante, seguimiento de reparaciones, cuenta del cliente, "Trabajá con nosotros".
- **El sistema de gestión** (`sistema/`): lo que uso yo en el local. Reparaciones, pedidos, productos con fotos, precios, promociones, clientes, stock con reservas, instalaciones y la planilla de abonos de monitoreo.
- **El backend** (`backend/`): la API en Java que usan las dos, con la base de datos.

La idea a futuro es venderlo a otros talleres, por eso ya está pensado para varios talleres (multi-tenant) aunque hoy lo use solo RAM.

## Tecnologías

**Backend**
- Java 25 con Spring Boot 4.1
- Spring Modulith (el backend está dividido en módulos y un test verifica que no se mezclen)
- Arquitectura hexagonal en cada módulo (dominio, aplicación y adaptadores)
- Spring Security con sesiones guardadas en la base, CSRF y roles (Dueño, Técnico, Mostrador y Cliente)
- Spring Data JPA con Hibernate 7
- PostgreSQL 18 y Flyway para las migraciones
- JavaMail para los avisos por mail
- JUnit 5, AssertJ y Testcontainers para los tests

**Tienda**
- Next.js 16 con React 19 y TypeScript
- Tailwind CSS 4

**Sistema de gestión**
- React 19 con Vite 8 y TypeScript
- React Router y TanStack Query
- Tailwind CSS 4 y oxlint

**Otros**
- Docker, para levantar PostgreSQL en desarrollo sin instalarlo
- Las decisiones de arquitectura están escritas como ADRs en `docs/adr/`

## Cómo está organizado

```
ram/
├── backend/    API en Spring Boot (puerto 8080)
├── web/        tienda en Next.js (puerto 3000)
├── sistema/    sistema de gestión en React + Vite (puerto 5173)
├── deploy/     lo que corre en el servidor (Docker Compose y Caddy)
└── docs/
    ├── adr/        por qué se hizo cada cosa así
    └── DEPLOY.md   cómo subirlo a internet
```

## Qué hace falta instalar

Probado en Windows con IntelliJ, pero anda igual en Linux o Mac.

| Programa | Versión | Para qué |
| --- | --- | --- |
| [Git](https://git-scm.com/) | cualquiera reciente | Clonar el repo |
| [JDK](https://adoptium.net/) | 25 | Correr el backend |
| [Node.js](https://nodejs.org/) | 22 o más nuevo (con npm) | La tienda y el sistema |
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | cualquiera reciente | La base de datos de desarrollo y los tests |

Maven no hace falta instalarlo, el proyecto trae su propio Maven (`mvnw`). PostgreSQL tampoco, lo levanta Docker solo.

Para ver si está todo:

```bash
git --version
java -version      # tiene que decir 25
node -v            # v22 o más
docker --version
```

## Correrlo en tu máquina desde cero

### 1. Clonar

```bash
git clone https://github.com/TU-USUARIO/ram.git
cd ram
```

### 2. Configurar el backend

Hay un archivo de configuración que **no está en el repo** a propósito, porque lleva claves. Creá `backend/config/application.yml` con esto:

```yaml
spring:
  profiles:
    active: dev        # carga productos de ejemplo para tener algo que ver

ram:
  dueno-inicial:
    taller: ram
    nombre: Tu Nombre
    correo: tu@correo.com
    clave: una-clave-larga-que-inventes
```

Con eso, la primera vez que arranca se crea el usuario Dueño para entrar al sistema.

Los mails son opcionales. Si no configurás nada, en vez de mandarse se escriben en la consola, que para probar alcanza. Si querés que lleguen de verdad (por ejemplo con Gmail y una contraseña de aplicación), agregá en el mismo archivo:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: tu@gmail.com
    password: "la-contraseña-de-aplicación"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
```

`backend/config/` ya está en el `.gitignore`, pero fijate igual de no subirlo.

### 3. Levantar el backend

Con Docker Desktop abierto:

```bash
cd backend
./mvnw spring-boot:run          # en Windows: mvnw.cmd spring-boot:run
```

Spring levanta solo el contenedor de PostgreSQL (`compose.yaml`) y Flyway crea las tablas. Queda en `http://localhost:8080`.

Desde IntelliJ también se puede: abrís la carpeta `backend` y le das Run a `RamApplication`.

### 4. Levantar la tienda

En otra terminal:

```bash
cd web
npm install
npm run dev
```

Entrá por **http://ram.localhost:3000** y no por `localhost:3000` a secas. El backend sabe de qué taller es cada pedido por el dominio, y con `localhost` solo no encuentra ninguno: se ven los productos, pero al crear una cuenta tira error. Los navegadores ya entienden `ram.localhost` sin configurar nada.

### 5. Levantar el sistema de gestión

En otra terminal:

```bash
cd sistema
npm install
npm run dev
```

Entrá a `http://localhost:5173` con el correo y la clave que pusiste en el paso 2.

El backend tiene que estar prendido para la tienda y para el sistema, también para hacer `npm run build` de la tienda.

## Tests

```bash
cd backend
./mvnw test
```

Los tests de integración levantan un PostgreSQL real con Testcontainers, así que Docker tiene que estar abierto. Hay un test (`ModulosTests`) que falla si un módulo usa clases internas de otro.

Para el front:

```bash
cd web && npm run lint
cd sistema && npm run lint
```

## Si algo no anda

- **"Algo salió mal" al crear una cuenta en la tienda**: estás entrando por `localhost:3000`. Usá `ram.localhost:3000`.
- **El backend no arranca y habla de Docker**: Docker Desktop está cerrado.
- **El backend no arranca y dice "Schema validation"**: la base no coincide con las entidades. Fijate que tengas todas las migraciones de `backend/src/main/resources/db/migration`.
- **"Hiciste muchos intentos"**: pusiste mal la clave 5 veces. Esperá 15 minutos o reiniciá el backend.
- **Puerto ocupado**: cerrá lo que esté usando el 8080, el 3000 o el 5173.

## Documentación

- `docs/adr/`: cada decisión importante, por qué se tomó y qué alternativas había. Son 17 y conviene leerlos en orden.
- `docs/DEPLOY.md`: cómo lo subí a internet, paso a paso (servidor con Docker, Caddy para el HTTPS y backups).
