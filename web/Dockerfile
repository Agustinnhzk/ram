# Tienda de RAM para producción.
# Ojo: "npm run build" NO se hace al armar la imagen sino al arrancar el contenedor, porque Next arma las
# páginas del catálogo pidiéndole los datos al backend, y el backend recién existe cuando arranca todo junto.
FROM node:24-slim
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ENV NODE_ENV=production NEXT_TELEMETRY_DISABLED=1
EXPOSE 3000
CMD ["sh", "-c", "node esperar-backend.mjs && npm run build && exec npx next start -H 0.0.0.0 -p 3000"]
