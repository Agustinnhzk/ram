/**
 * Las imágenes que sirve el backend (/api/publico/fotos/..., /api/publico/banners/...) ya vienen achicadas
 * y en JPEG: Next no las vuelve a procesar (unoptimized). Además, el optimizador de Next las pediría sin el
 * dominio del taller, y el backend no sabría de qué taller son.
 */
export function esImagenDeLaApi(src: string): boolean {
  return src.startsWith("/api/");
}
