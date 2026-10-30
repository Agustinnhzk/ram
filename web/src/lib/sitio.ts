/**
 * La dirección pública de la web, para armar links completos (los que se mandan por WhatsApp
 * o los que usan las redes para la vista previa). Al publicar, se define URL_DEL_SITIO.
 */
export const URL_DEL_SITIO = process.env.URL_DEL_SITIO ?? "http://localhost:3000";

/** "/arma-tu-pc?cpu=..." → "https://ram.com.ar/arma-tu-pc?cpu=..." */
export function urlCompleta(ruta: string): string {
  return new URL(ruta, URL_DEL_SITIO).toString();
}
