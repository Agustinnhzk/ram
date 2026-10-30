# Puesta en línea (deploy)

Lo que hay que hacer para pasar RAM de tu compu a internet. Se completa cuando se elija el hosting.

## Cómo queda armado

```
Internet ──HTTPS──▶ Nginx (en el VPS)
                      ├── ramtudominio.com.ar          → tienda (Next.js, puerto 3000)
                      │      └── /api/...              → backend (Spring, puerto 8080)
                      └── sistema.ramtudominio.com.ar  → sistema (archivos de `npm run build`)
                             └── /api/...              → backend (Spring, puerto 8080)
Backend ──▶ PostgreSQL (en el mismo VPS, solo accesible desde adentro)
```

El backend y la base **no** quedan expuestos a internet: solo Nginx (puertos 80 y 443).

## Checklist

### Antes
- [ ] VPS con Linux (2 GB de RAM alcanzan), con Java 25, Node 24, PostgreSQL y Nginx.
- [ ] Dominio comprado y apuntado al VPS (registros A del dominio y de `sistema.`).
- [ ] Certificado HTTPS con Let's Encrypt (`certbot --nginx`).
- [ ] Casilla de mail del dominio creada, y en el DNS los registros **SPF, DKIM y DMARC** que indique el hosting.

### Configuración del backend (variables de entorno, nunca en git)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ram
SPRING_DATASOURCE_USERNAME=ram
SPRING_DATASOURCE_PASSWORD=...           # una clave larga y nueva
SPRING_MAIL_HOST=mail.tudominio.com.ar   # lo indica el hosting
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=reparaciones@tudominio.com.ar
SPRING_MAIL_PASSWORD=...
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
RAM_CORREO_DESTINO_DEL_TALLER=reparaciones@tudominio.com.ar   # a dónde llegan los avisos
RAM_PLATAFORMA_DOMINIO_BASE=tudominio.com.ar
```

Sin el perfil `dev` (así no se cargan los productos de ejemplo). El dueño inicial, con una clave nueva.

### Datos del taller en la base
- [ ] Cargar el dominio propio del taller RAM (tabla `talleres_dominios`), para que el backend lo reconozca.

### Tienda
- [ ] `TALLER_DOMINIO=tudominio.com.ar` y `BACKEND_URL=http://localhost:8080` al compilar y al correr.
- [ ] El mail de contacto: en `web/src/lib/api/datos-de-ejemplo.ts`, `correo: "reparaciones@tudominio.com.ar"`.
      Aparece solo en el pie, Quiénes somos y Servicios.

### Después
- [ ] Backup diario de la base (`pg_dump`) guardado fuera del VPS.
- [ ] Compras de prueba → borrarlas desde el sistema (ADR-016).
- [ ] Probar "olvidé mi contraseña" y un pedido: que lleguen los mails (y no a spam).
