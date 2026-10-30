import type { NextConfig } from "next";

/** Dónde está la API de Spring. En tu máquina, localhost:8080. */
const backend = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  cacheComponents: true,
  /**
   * Lo que el navegador pida a /api/... lo pasa Next a Spring (un "proxy"). Así la tienda y la API
   * comparten dominio: sin CORS y con las cookies funcionando.
   *
   * El backend elige el taller por el dominio (ADR-002): Next le manda el original en X-Forwarded-Host
   * (por eso en desarrollo se entra por http://ram.localhost:3000) y Spring lo acepta porque viene de
   * un proxy de confianza (server.forward-headers-strategy: native).
   *
   * En producción, el servidor web manda /api directo a Spring y esto ni se usa.
   */
  async rewrites() {
    return [{ source: "/api/:ruta*", destination: `${backend}/api/:ruta*` }];
  },
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
