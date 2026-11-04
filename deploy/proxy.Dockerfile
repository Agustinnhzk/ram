# La puerta de entrada: Caddy (HTTPS automático) + el sistema de gestión ya compilado.
# Se arma desde la raíz del repo (ver compose.yaml: context ..).

# Etapa 1: compila el sistema (React + Vite) a archivos estáticos.
FROM node:24-slim AS sistema
WORKDIR /sistema
COPY sistema/package.json sistema/package-lock.json ./
RUN npm ci
COPY sistema/ .
# El link "ver en la tienda" del sistema.
ARG URL_TIENDA
ENV VITE_URL_TIENDA=${URL_TIENDA}
RUN npm run build

# Etapa 2: Caddy sirve el sistema y reparte el resto (tienda y API).
FROM caddy:2
COPY deploy/Caddyfile /etc/caddy/Caddyfile
COPY --from=sistema /sistema/dist /srv/sistema
