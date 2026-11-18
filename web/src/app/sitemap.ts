import type { MetadataRoute } from "next";
import { obtenerCategorias, obtenerSlugsDeProductos, obtenerTrabajos } from "@/lib/api";
import { urlDelCatalogo } from "@/lib/catalogo";
import { urlCompleta } from "@/lib/sitio";

/**
 * /sitemap.xml: la lista de páginas que queremos que Google encuentre.
 *
 * Van las páginas públicas, las categorías del catálogo, cada producto publicado y cada trabajo realizado
 * publicado (salen de la API, así que uno nuevo entra solo). No van las páginas con robots: { index: false }
 * (carrito, checkout, cuenta, seguimiento).
 */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const [categorias, slugs, trabajos] = await Promise.all([obtenerCategorias(), obtenerSlugsDeProductos(), obtenerTrabajos()]);

  const fijas: MetadataRoute.Sitemap = [
    { url: urlCompleta("/"), changeFrequency: "weekly", priority: 1 },
    { url: urlCompleta("/servicios"), changeFrequency: "monthly", priority: 0.9 },
    { url: urlCompleta("/productos"), changeFrequency: "daily", priority: 0.9 },
    { url: urlCompleta("/arma-tu-pc"), changeFrequency: "weekly", priority: 0.8 },
    { url: urlCompleta("/quienes-somos"), changeFrequency: "yearly", priority: 0.6 },
    { url: urlCompleta("/trabaja-con-nosotros"), changeFrequency: "yearly", priority: 0.4 },
  ];

  const deCategorias: MetadataRoute.Sitemap = categorias.map((c) => ({
    url: urlCompleta(urlDelCatalogo({ categoria: c.slug })),
    changeFrequency: "daily",
    priority: 0.8,
  }));

  // "sin-productos" es el slug de relleno que se usa cuando el catálogo está vacío (ver lib/api).
  const deProductos: MetadataRoute.Sitemap = slugs
    .filter((slug) => slug !== "sin-productos")
    .map((slug) => ({ url: urlCompleta(`/productos/${slug}`), changeFrequency: "weekly", priority: 0.7 }));

  // Los trabajos realizados, con la fecha en que se hicieron. /trabajos entra solo si ya hay alguno (si no, es una página vacía).
  const deTrabajos: MetadataRoute.Sitemap = trabajos.length
    ? [
        { url: urlCompleta("/trabajos"), changeFrequency: "weekly", priority: 0.8, lastModified: trabajos[0].fecha },
        ...trabajos.map((t) => ({ url: urlCompleta(`/trabajos/${t.slug}`), changeFrequency: "monthly" as const, priority: 0.6, lastModified: t.fecha })),
      ]
    : [];

  return [...fijas, ...deCategorias, ...deProductos, ...deTrabajos];
}
