import type { Categoria, Condicion, FiltrosCatalogo } from "@/lib/api";

/**
 * Lo que tiene que ver con la URL del catálogo: leer los filtros y armar links con filtros.
 *
 * La URL la puede escribir cualquiera (/productos?categoria=<script>&q=...), así que nada de lo
 * que viene ahí se usa sin revisar: lo que no es válido se ignora, como si no estuviera.
 */

export type ParametrosDeUrl = Record<string, string | string[] | undefined>;

const CONDICIONES: Condicion[] = ["nuevo", "reacondicionado"];
const LARGO_MAXIMO_BUSQUEDA = 80;

export function leerFiltros(parametros: ParametrosDeUrl, categorias: Categoria[]): FiltrosCatalogo {
  const categoria = primero(parametros.categoria);
  const condicion = primero(parametros.condicion);
  const q = primero(parametros.q)?.trim().slice(0, LARGO_MAXIMO_BUSQUEDA);

  return {
    categoria: categorias.some((c) => c.slug === categoria) ? categoria : undefined,
    condicion: CONDICIONES.includes(condicion as Condicion) ? (condicion as Condicion) : undefined,
    q: q || undefined,
    oferta: primero(parametros.oferta) === "si" || undefined,
  };
}

/** Link al catálogo con estos filtros. Los vacíos no se escriben: /productos?categoria=camaras, no ?categoria=camaras&q= */
export function urlDelCatalogo(filtros: FiltrosCatalogo): string {
  const parametros = new URLSearchParams();
  if (filtros.categoria) parametros.set("categoria", filtros.categoria);
  if (filtros.condicion) parametros.set("condicion", filtros.condicion);
  if (filtros.q) parametros.set("q", filtros.q);
  if (filtros.oferta) parametros.set("oferta", "si");
  const texto = parametros.toString();
  return texto ? `/productos?${texto}` : "/productos";
}

export function hayFiltros(filtros: FiltrosCatalogo): boolean {
  return Boolean(filtros.categoria || filtros.condicion || filtros.q || filtros.oferta);
}

/** ?categoria=a&categoria=b llega como lista: nos quedamos con el primero. */
export function primero(valor: string | string[] | undefined): string | undefined {
  return Array.isArray(valor) ? valor[0] : valor;
}
