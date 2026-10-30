import { URL_TIENDA } from "./reparaciones";

/**
 * Las fotos subidas vienen del backend (/api/...: pasan por el proxy). Las "heredadas" (datos de ejemplo,
 * flyers viejos) son archivos de la tienda (/promos/..., /productos/...): se piden a la tienda.
 */
export function urlDeImagen(src: string): string {
  return src.startsWith("/api/") || src.startsWith("http") ? src : `${URL_TIENDA}${src}`;
}
