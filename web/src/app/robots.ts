import type { MetadataRoute } from "next";
import { urlCompleta } from "@/lib/sitio";

/**
 * /robots.txt: qué puede recorrer Google.
 *
 * - La API no es contenido, salvo las fotos del catálogo (/api/publico/fotos/...): si se bloquean,
 *   Google no puede mostrar los productos en Google Imágenes.
 * - El carrito, el checkout, la cuenta y el seguimiento NO se bloquean acá: ya tienen
 *   robots: { index: false } en su metadata. Si se bloquearan, Google no podría entrar a leer
 *   ese "no indexar" y las podría seguir mostrando (sin contenido) si alguien las enlaza.
 */
export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: ["/", "/api/publico/fotos/"],
      disallow: "/api/",
    },
    sitemap: urlCompleta("/sitemap.xml"),
  };
}
