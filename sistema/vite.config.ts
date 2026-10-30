import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

/**
 * En desarrollo, Vite sirve el sistema en http://localhost:5173 y le pasa a Spring todo lo que empiece con /api.
 *
 * ¿Por qué un proxy y no llamar directo a localhost:8080?
 * - El navegador ve un solo origen (localhost:5173): la cookie de sesión y la del token CSRF funcionan
 *   igual que en producción, donde el sistema y la API comparten dominio (ADR-007). Sin CORS.
 * - El backend elige el taller por el Host (ADR-002): el proxy le dice "soy ram.localhost:8080".
 *
 * Variables opcionales: BACKEND_URL (por defecto http://localhost:8080) y TALLER (por defecto "ram").
 */
const backend = process.env.BACKEND_URL ?? "http://localhost:8080";
const taller = process.env.TALLER ?? "ram";
const puertoBackend = new URL(backend).port || "80";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      "/api": {
        target: backend,
        configure: (proxy) => {
          proxy.on("proxyReq", (pedido) => pedido.setHeader("Host", `${taller}.localhost:${puertoBackend}`));
        },
      },
    },
  },
});
