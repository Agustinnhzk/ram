import { cacheLife } from "next/cache";
import { gruposDeServicios, modelosDePc, quienesSomos, taller } from "./datos-de-ejemplo";
import type {
  Categoria,
  Componente,
  Disponibilidad,
  FiltrosCatalogo,
  GrupoDeServicios,
  ModeloDePc,
  Producto,
  Promocion,
  QuienesSomos,
  Taller,
} from "./tipos";

/**
 * Único punto de acceso a los datos para toda la web.
 *
 * El catálogo, los componentes, los banners y el precio del armado vienen del backend (Spring), así lo que
 * se carga en el sistema se ve en la tienda. Lo demás (servicios, quiénes somos, modelos de PC) sigue en
 * ./datos-de-ejemplo hasta que tenga su módulo.
 *
 * "use cache": Next.js guarda el resultado y no lo vuelve a pedir en cada visita. Con DATOS_FRESCOS:
 * - revalidate 30: el servidor lo vuelve a pedir al backend cada 30 segundos → un precio cambiado en el
 *   sistema aparece en la tienda en menos de un minuto (al entrar o recargar).
 * - stale 300: lo que el navegador ya tiene sirve 5 minutos mientras se navega sin recargar. Menos que eso,
 *   Next lo trata como "dato que cambia en cada visita" y no puede armar la página de antemano.
 */

const DATOS_FRESCOS = { stale: 300, revalidate: 30, expire: 3600 };

/** Dónde está Spring. Es el mismo que usa el proxy de /api (next.config.ts). */
const BACKEND = process.env.BACKEND_URL ?? "http://localhost:8080";

/**
 * De qué taller es esta tienda. El backend elige el taller por el dominio (ADR-002): cuando el pedido lo hace
 * el servidor de Next (no el navegador), se lo dice en X-Forwarded-Host. Spring lo acepta porque viene de
 * un proxy de confianza (localhost o la red interna, server.forward-headers-strategy: native).
 * En producción: el dominio de la tienda (TALLER_DOMINIO=ramservicios.com.ar).
 */
const DOMINIO = process.env.TALLER_DOMINIO ?? "ram.localhost";

async function desdeLaApi<T>(ruta: string): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await fetch(`${BACKEND}${ruta}`, {
      headers: { Accept: "application/json", "X-Forwarded-Host": DOMINIO, "X-Forwarded-For": "127.0.0.1", "X-Forwarded-Proto": "http" },
    });
  } catch (error) {
    throw new Error(`No se pudo conectar con el backend en ${BACKEND} (¿está prendido?). Pedido: ${ruta}`, { cause: error });
  }
  if (!respuesta.ok) throw new Error(`El backend respondió ${respuesta.status} a ${ruta}`);
  return (await respuesta.json()) as T;
}

/** Igual, pero un 404 es "no existe" (null), no un error. */
async function desdeLaApiOpcional<T>(ruta: string): Promise<T | null> {
  try {
    return await desdeLaApi<T>(ruta);
  } catch (error) {
    if (error instanceof Error && error.message.includes(" 404 ")) return null;
    throw error;
  }
}

export async function obtenerTaller(): Promise<Taller> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const armado = await desdeLaApiOpcional<{ precio: number }>("/api/publico/armado-de-pc");
  return { ...taller, precioArmado: armado?.precio ?? null };
}

/** Los banners del carrusel de la portada (se cargan en el sistema, en Promociones). */
export async function obtenerPromociones(): Promise<Promocion[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  return desdeLaApi<Promocion[]>("/api/publico/banners");
}

/** El stock de los productos que lo controlan, por slug (ADR-014). Los que no están, no se controlan. */
async function stockPorSlug(): Promise<Map<string, Disponibilidad>> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const lista = await desdeLaApi<(Disponibilidad & { slug: string })[]>("/api/publico/stock");
  return new Map(lista.map(({ slug, estado, quedan }) => [slug, { estado, quedan }]));
}

/** Le pega a cada producto (o componente) su stock. */
async function conStock<T extends { slug: string }>(items: T[]): Promise<(T & { stock: Disponibilidad | null })[]> {
  const stock = await stockPorSlug();
  return items.map((item) => ({ ...item, stock: stock.get(item.slug) ?? null }));
}

/** Todos los publicados, en el orden del catálogo. */
async function todosLosProductos(): Promise<Producto[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  return conStock(await desdeLaApi<Producto[]>("/api/publico/productos"));
}

/** Los que están en oferta primero: es lo que más conviene mostrar en la portada. */
export async function obtenerProductosDestacados(cantidad = 8): Promise<Producto[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const productos = await todosLosProductos();
  const enOferta = productos.filter((p) => p.precioAnterior !== null);
  return [...enOferta, ...productos.filter((p) => p.precioAnterior === null)].slice(0, cantidad);
}

/**
 * Las categorías que tienen algo publicado. (La de componentes, por ejemplo, existe para Armá tu PC
 * pero no tiene productos sueltos: no tiene sentido mostrarla como filtro vacío.)
 */
export async function obtenerCategorias(): Promise<Categoria[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const [categorias, productos] = await Promise.all([desdeLaApi<Categoria[]>("/api/publico/categorias"), todosLosProductos()]);
  const conProductos = new Set(productos.map((p) => p.categoria));
  return categorias.filter((c) => conProductos.has(c.slug));
}

/** El catálogo filtrado: GET /api/publico/productos?categoria=...&condicion=...&q=... (la oferta se filtra acá). */
export async function buscarProductos(filtros: FiltrosCatalogo): Promise<Producto[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const parametros = new URLSearchParams();
  if (filtros.categoria) parametros.set("categoria", filtros.categoria);
  if (filtros.condicion) parametros.set("condicion", filtros.condicion);
  if (filtros.q) parametros.set("q", filtros.q);
  const productos = await conStock(await desdeLaApi<Producto[]>(`/api/publico/productos${parametros.size ? `?${parametros}` : ""}`));
  return filtros.oferta ? productos.filter((p) => p.precioAnterior !== null) : productos;
}

/** null si no existe o no está publicado (la página muestra "no encontrado"). */
export async function obtenerProducto(slug: string): Promise<Producto | null> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const producto = await desdeLaApiOpcional<Producto>(`/api/publico/productos/${encodeURIComponent(slug)}`);
  return producto && (await conStock([producto]))[0];
}

/** Otros productos de la misma categoría, para "También te puede interesar". */
export async function obtenerRelacionados(producto: Producto, cantidad = 4): Promise<Producto[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const productos = await todosLosProductos();
  return productos.filter((p) => p.categoria === producto.categoria && p.id !== producto.id).slice(0, cantidad);
}

/**
 * Los slugs de los productos: Next.js arma de antemano la página de cada uno.
 * Next pide al menos uno: si el catálogo está vacío, va uno que no existe (y da "no encontrado").
 */
export async function obtenerSlugsDeProductos(): Promise<string[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  const slugs = (await todosLosProductos()).map((p) => p.slug);
  return slugs.length ? slugs : ["sin-productos"];
}

/** Futuro: GET /api/publico/servicios (se cargan desde el sistema de gestión). */
export async function obtenerServicios(): Promise<GrupoDeServicios[]> {
  "use cache";
  cacheLife("hours");
  return gruposDeServicios;
}

/** Futuro: GET /api/publico/quienes-somos. */
export async function obtenerQuienesSomos(): Promise<QuienesSomos> {
  "use cache";
  cacheLife("hours");
  return quienesSomos;
}

/** Todos los de Armá tu PC (publicados o no como productos sueltos): la página los filtra por compatibilidad. */
export async function obtenerComponentes(): Promise<Componente[]> {
  "use cache";
  cacheLife(DATOS_FRESCOS);
  return conStock(await desdeLaApi<Componente[]>("/api/publico/componentes"));
}

/** Futuro: GET /api/publico/modelos-de-pc. */
export async function obtenerModelosDePc(): Promise<ModeloDePc[]> {
  "use cache";
  cacheLife("hours");
  return modelosDePc;
}

export type {
  Categoria,
  Componente,
  Condicion,
  Disponibilidad,
  FormatoMother,
  FiltrosCatalogo,
  GrupoDeServicios,
  IconoDeServicio,
  ModeloDePc,
  Producto,
  Promocion,
  QuienesSomos,
  Servicio,
  Taller,
  TipoComponente,
  TipoRam,
} from "./tipos";
