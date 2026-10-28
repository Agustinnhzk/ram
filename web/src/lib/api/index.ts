import { cacheLife } from "next/cache";
import { productos, promociones, taller } from "./datos-de-ejemplo";
import type { Producto, Promocion, Taller } from "./tipos";

/**
 * Único punto de acceso a los datos para toda la web.
 *
 * Hoy devuelve datos de ejemplo. Cuando existan los módulos de catálogo y contenido,
 * solo cambia el CUERPO de estas funciones (por un fetch a /api/publico/...);
 * las pantallas no se enteran, porque reciben exactamente los mismos tipos.
 *
 * "use cache": Next.js guarda el resultado y no vuelve a pedirlo en cada visita.
 * cacheLife("hours"): el resultado se renueva cada algunas horas.
 */

export async function obtenerTaller(): Promise<Taller> {
  "use cache";
  cacheLife("hours");
  return taller;
}

export async function obtenerPromociones(): Promise<Promocion[]> {
  "use cache";
  cacheLife("hours");
  return promociones;
}

export async function obtenerProductosDestacados(cantidad = 8): Promise<Producto[]> {
  "use cache";
  cacheLife("hours");
  return productos.slice(0, cantidad);
}

export type { Producto, Promocion, Taller } from "./tipos";
