import type { Producto } from "@/lib/api";
import { formatearPrecio } from "@/lib/formato";

const dia = new Intl.DateTimeFormat("es-AR", { day: "numeric", month: "numeric", timeZone: "UTC" });

/**
 * El precio, y si está en oferta, el de lista tachado y hasta cuándo dura.
 * <del> hace que el lector de pantalla diga "eliminado": se aclara con texto oculto cuál es cuál.
 */
export function PrecioConOferta({ producto, grande = false }: { producto: Producto; grande?: boolean }) {
  if (producto.precio === null) return null;
  const tamano = grande ? "text-4xl" : "text-xl";
  return (
    <div>
      {producto.precioAnterior !== null && (
        <p className={`text-suave ${grande ? "text-lg" : "text-sm"}`}>
          <span className="sr-only">Antes: </span>
          <del>{formatearPrecio(producto.precioAnterior)}</del>
          <span className="ml-2 rounded-full bg-oro/15 px-2 py-0.5 text-xs font-semibold text-oro no-underline">
            −{Math.round((1 - producto.precio / producto.precioAnterior) * 100)}%
          </span>
        </p>
      )}
      <p className={`font-display font-bold text-principal ${tamano}`}>
        {producto.precioAnterior !== null && <span className="sr-only">Ahora: </span>}
        {formatearPrecio(producto.precio)}
      </p>
      {grande && producto.ofertaHasta && (
        // La fecha viene como "2026-11-30": se muestra sin pasar por la zona horaria del navegador.
        <p className="mt-1 text-sm text-oro">Oferta válida hasta el {dia.format(new Date(`${producto.ofertaHasta}T00:00:00Z`))}</p>
      )}
    </div>
  );
}
