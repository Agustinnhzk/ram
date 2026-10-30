# Sistema de gestión de R.A.M.

React + TypeScript + Vite (ver `docs/adr/0007`). Le habla directo a la API de Spring (`/api/sistema/**`).

## Correrlo en tu máquina

1. Levantá el backend (Spring en `http://localhost:8080`, con Docker andando).
2. En esta carpeta:

   ```
   npm install
   npm run dev
   ```

3. Abrí `http://localhost:5173` e ingresá con el usuario dueño de `backend/config/application.yml`.

Vite le pasa a Spring todo lo que empiece con `/api`, diciéndole que es el taller `ram` (ver `vite.config.ts`).
Para otro backend o taller: `BACKEND_URL=http://localhost:9090 TALLER=otro npm run dev`
(en PowerShell: `$env:BACKEND_URL="..."; npm run dev`).

## Al publicar

`VITE_URL_TIENDA` es la dirección de la tienda, para el link de seguimiento que va en el comprobante
y en los WhatsApp (por defecto `http://localhost:3000`). Se define al compilar:
`VITE_URL_TIENDA=https://tudominio.com.ar npm run build`.

## Comandos

| Comando | Qué hace |
| --- | --- |
| `npm run dev` | Servidor de desarrollo con recarga en caliente |
| `npm run build` | Revisa los tipos y genera `dist/` (archivos estáticos para publicar) |
| `npm run lint` | Busca errores comunes en el código (oxlint) |

## Estructura

```
src/
├── api/          cliente.ts (sesión, CSRF, errores) + un archivo por tema (sesion, precios)
├── componentes/  marco con el menú, ruta protegida, diálogos, avisos, íconos
├── paginas/      una por pantalla (Ingresar, Inicio, Precios, reparaciones/…)
└── utilidades/   formato de precios y fechas, texto para buscar
```
