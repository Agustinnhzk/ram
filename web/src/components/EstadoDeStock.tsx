import type { Disponibilidad } from "@/lib/api";

/**
 * Lo que dice la tienda del stock (ADR-014). Si el taller no controla el stock del producto (null), no dice nada.
 * Sin stock no es "no se puede comprar": se compra igual y queda reservado hasta que llegue.
 */
export function EstadoDeStock({ stock, className = "" }: { stock: Disponibilidad | null; className?: string }) {
  if (!stock) return null;
  if (stock.estado === "SIN_STOCK") {
    return <span className={`inline-block rounded-full border border-aviso/70 px-2.5 py-0.5 text-xs font-semibold text-aviso ${className}`}>Sin stock · se reserva</span>;
  }
  if (stock.estado === "ULTIMAS_UNIDADES") {
    const texto = stock.quedan === 1 ? "¡Última unidad!" : `Últimas ${stock.quedan}`;
    return <span className={`inline-block rounded-full border border-oro/70 px-2.5 py-0.5 text-xs font-semibold text-oro ${className}`}>{texto}</span>;
  }
  return <span className={`inline-block rounded-full border border-exito/60 px-2.5 py-0.5 text-xs text-exito ${className}`}>En stock</span>;
}

export function sinStock(stock: Disponibilidad | null): boolean {
  return stock?.estado === "SIN_STOCK";
}
