# Subir RAM a internet

Esto es lo que hice para poner RAM online en un Cloud Server de DonWeb con el dominio `ramtecnologias.com.ar`. Sirve igual para cualquier VPS con Ubuntu y Docker (Hostinger, Oracle Cloud, etc.).

## Cómo queda armado

Todo corre en un solo servidor con Docker. Lo único que da a internet es Caddy, que además saca solo los certificados HTTPS (el candadito) y los renueva.

```
internet ──HTTPS──▶ Caddy (puertos 80 y 443)
                      ├── ramtecnologias.com.ar          → tienda (Next)
                      │      └── /api/...                → backend (Spring)
                      ├── www.ramtecnologias.com.ar      → redirige a la de arriba
                      └── sistema.ramtecnologias.com.ar  → sistema de gestión (archivos estáticos)
                             └── /api/...                → backend (Spring)

backend ──▶ PostgreSQL   (los dos solo se ven desde adentro del servidor)
```

Los archivos están en `deploy/`:

| Archivo | Para qué |
| --- | --- |
| `compose.yaml` | Levanta los 4 contenedores: base, backend, tienda y Caddy |
| `Caddyfile` | Qué dominio va a dónde |
| `proxy.Dockerfile` | Compila el sistema y lo mete en Caddy |
| `.env.ejemplo` | Dominio y clave de la base (se copia como `.env`) |
| `application.ejemplo.yml` | Dueño inicial y mail (se copia como `application.yml`) |
| `actualizar.sh` | Baja lo nuevo de GitHub y reinicia |
| `backup.sh` | Copia de seguridad de la base |

Más los `Dockerfile` de `backend/` y `web/`. El `.env` y el `application.yml` del servidor tienen claves y están en el `.gitignore`.

---

## 0. Antes de empezar, en tu compu

1. En `backend/.gitignore` cambiá la línea `config/` por `/config/`. Con la barra solo ignora la carpeta del `application.yml`; sin la barra ignoraba también los paquetes `adaptadores/config` del código y el servidor no arrancaba.
2. Copiá al repo la carpeta `deploy/`, los `Dockerfile` y `.dockerignore` de `backend/`, `web/` y `sistema/`, `web/esperar-backend.mjs` y el `.gitattributes` de la raíz.
3. Fijate con `git status` que **no** aparezcan `backend/config/application.yml`, `deploy/.env` ni `deploy/application.yml`. Si aparece alguno, frená.
4. Commit y push.

## 1. Crear el servidor en DonWeb

- Cloud & IaaS → Cloud Server con **Docker** preinstalado, Ubuntu.
- 4 GB de RAM alcanza para arrancar (se puede agrandar después sin reinstalar).
- Anotá la **IP** del servidor y la **clave de root** que te da.

## 2. Apuntar el dominio al servidor

En DonWeb → Dominios → `ramtecnologias.com.ar` → zona DNS, creá tres registros tipo **A**, todos con la IP del servidor:

| Tipo | Nombre | Valor |
| --- | --- | --- |
| A | `@` (el dominio pelado) | IP del servidor |
| A | `www` | IP del servidor |
| A | `sistema` | IP del servidor |

Puede tardar desde minutos hasta unas horas. Para ver si ya está, en tu compu (PowerShell):

```
nslookup ramtecnologias.com.ar
nslookup sistema.ramtecnologias.com.ar
```

Cuando las dos respuestas muestran la IP del servidor, seguís. Si arrancás antes, Caddy no puede sacar los certificados (ver "Si algo no anda").

## 3. Entrar al servidor

Desde PowerShell:

```
ssh root@IP-DEL-SERVIDOR
```

La primera vez pregunta si confiás en el servidor: `yes`. Después la clave de root (no se ve mientras la escribís, es normal).

## 4. Preparar el servidor

```bash
# Actualizar Ubuntu
apt update && apt upgrade -y

# 2 GB de memoria extra en disco (swap), para que compilar no se quede sin RAM
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# Firewall: solo SSH y web
ufw allow OpenSSH && ufw allow 80/tcp && ufw allow 443 && ufw --force enable

# Ver que Docker esté (viene instalado con la plantilla)
docker compose version
```

Si `docker compose version` da error, Docker no vino instalado: `curl -fsSL https://get.docker.com | sh` y listo.

Si DonWeb tiene un firewall propio en el panel del Cloud Server, abrí ahí también los puertos 22, 80 y 443.

## 5. Bajar el código

**Si el repo es público:**

```bash
cd /root
git clone https://github.com/TU-USUARIO/ram.git
```

**Si el repo es privado**, el servidor necesita permiso para leerlo. Se hace con una "deploy key" (una llave que solo puede leer ese repo):

```bash
ssh-keygen -t ed25519 -f ~/.ssh/github -N ""
cat ~/.ssh/github.pub
```

Copiá lo que muestra y pegalo en GitHub → el repo → Settings → Deploy keys → Add deploy key (sin tildar "Allow write access"). Después:

```bash
printf 'Host github.com\n  IdentityFile ~/.ssh/github\n' >> ~/.ssh/config
cd /root
git clone git@github.com:TU-USUARIO/ram.git
```

## 6. Configurar las claves

```bash
cd /root/ram/deploy
cp .env.ejemplo .env
cp application.ejemplo.yml application.yml
openssl rand -hex 24      # te da una clave al azar para la base, copiala
nano .env                 # pegá la clave en POSTGRES_PASSWORD
nano application.yml      # tu nombre, tu correo, una clave nueva para entrar al sistema
chmod 600 .env application.yml   # que solo root pueda leerlos
```

En `nano` se guarda con `Ctrl+O`, `Enter`, y se sale con `Ctrl+X`.

La clave de `POSTGRES_PASSWORD` ponela ahora y no la cambies después: la base se crea con esa en el primer arranque.

## 7. Primer arranque

```bash
cd /root/ram/deploy
docker compose up -d --build
```

La primera vez tarda (entre 5 y 15 minutos): baja Java, Node y PostgreSQL y compila todo.

Cuando termine, esperá a que arranque el backend:

```bash
docker compose logs -f backend
```

Cuando aparece una línea con `Started RamApplication`, salí con `Ctrl+C` y cargá el dominio del taller (esto es una sola vez):

```bash
docker compose exec db psql -U ram ram -c "INSERT INTO talleres_dominios (dominio, taller_id) VALUES ('ramtecnologias.com.ar', '00000000-0000-0000-0000-000000000001');"
```

Esto le dice al backend que `ramtecnologias.com.ar` es el taller RAM (ADR-002). Hasta que no lo hacés, la tienda se queda esperando.

Después mirá la tienda:

```bash
docker compose logs -f web
```

Va a decir "Backend listo. Compilando la tienda..." y al rato `Ready`. Ahí ya está.

## 8. Probar

- https://ramtecnologias.com.ar → la tienda, con el candadito.
- https://www.ramtecnologias.com.ar → te manda a la de arriba.
- https://sistema.ramtecnologias.com.ar → el login del sistema. Entrás con el correo y la clave del `application.yml`.
- Cargá un producto en el sistema y fijate que aparezca en la tienda (tarda hasta 30 segundos).
- Hacé un pedido de prueba y después borralo desde el sistema (ADR-016).

El dueño inicial se crea solo la primera vez (cuando el taller no tiene personal). Una vez que entraste, podés borrar la línea `clave:` del `application.yml` del servidor.

## 9. Backups

Programar uno por día a las 4 de la mañana:

```bash
crontab -e
```

(si pregunta el editor, elegí `nano`) y agregá al final:

```
0 4 * * * sh /root/ram/deploy/backup.sh >> /root/backup.log 2>&1
```

Quedan en `/root/ram/deploy/backups/`, los últimos 14 días. Si se rompe el servidor, se pierden con él, así que cada tanto bajate el último a tu compu (desde PowerShell, en tu compu):

```
scp root@IP-DEL-SERVIDOR:/root/ram/deploy/backups/ram-*.sql.gz .
```

O con WinSCP / FileZilla en modo SFTP, arrastrando.

Para restaurar uno (en una base vacía):

```bash
gunzip -c backups/ram-2026-11-03-0400.sql.gz | docker compose exec -T db psql -U ram ram
```

## 10. Actualizar cuando cambio algo

En tu compu: commit y push. En el servidor:

```bash
sh /root/ram/deploy/actualizar.sh
```

Baja lo nuevo, recompila lo que cambió y reinicia. La tienda tarda uno o dos minutos en volver porque se recompila al arrancar.

## 11. El mail (cuando lo tenga)

1. Crear la casilla (`reparaciones@ramtecnologias.com.ar`) en DonWeb.
2. En la zona DNS, cargar los registros **SPF, DKIM y DMARC** que te indique DonWeb. Sin eso los mails caen en spam.
3. En el servidor, descomentar el bloque `spring.mail` de `deploy/application.yml` con los datos SMTP, y en `ram.correo.destino-del-taller` poner el mail nuevo.
4. `docker compose restart backend`.
5. En `web/src/lib/api/datos-de-ejemplo.ts`, `correo: "reparaciones@ramtecnologias.com.ar"`, y actualizar.
6. Probar "olvidé mi contraseña" y un pedido: que lleguen los mails.

---

## Comandos útiles

Todos parado en `/root/ram/deploy`:

| Para | Comando |
| --- | --- |
| Ver qué está corriendo | `docker compose ps` |
| Ver los logs de algo | `docker compose logs -f backend` (o `web`, `proxy`, `db`) |
| Reiniciar algo | `docker compose restart backend` |
| Apagar todo | `docker compose down` |
| Prender todo | `docker compose up -d` |
| Ver el disco | `df -h` |
| Entrar a la base | `docker compose exec db psql -U ram ram` |

**Nunca** uses `docker compose down -v` ni `docker system prune --volumes`: la `-v` / `--volumes` borra la base de datos.

## Si algo no anda

- **El navegador dice que el sitio no es seguro / no hay certificado**: el DNS todavía no apuntaba al servidor cuando arrancó Caddy, o el puerto 80 está cerrado. Revisá el paso 2 y el firewall, y `docker compose restart proxy`. Mirá `docker compose logs proxy`.
- **"502 Bad Gateway" en la tienda**: la tienda se está compilando. Esperá y mirá `docker compose logs -f web`.
- **La tienda dice "El backend responde pero no reconoce ramtecnologias.com.ar"**: falta el `INSERT` del paso 7.
- **El backend no arranca y habla de `schema validation`**: falta alguna migración en el repo. Fijate que esté todo pusheado.
- **El backend dice que no encuentra un bean (`required a bean of type ...`)**: el `.gitignore` sigue con `config/` sin la barra (paso 0).
- **`/bin/sh^M: not found` o `mvnw: not found`**: los scripts tienen fin de línea de Windows. El `.gitattributes` lo evita; si pasó igual, `git add --renormalize .` en tu compu, commit y push.
- **Se llenó el disco**: `docker image prune -f` borra imágenes viejas.
