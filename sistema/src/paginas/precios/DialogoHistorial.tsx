import { useHistorial, type CambioDePrecio, type ProductoConPrecio } from "../../api/precios";
import { Dialogo } from "../../componentes/Dialogo";
import { formatearFecha, formatearPrecio } from "../../utilidades/formato";

/** Los últimos cambios de precio de un producto: cuándo, de cuánto a cuánto, por qué y quién. */
export function DialogoHistorial({ producto, alCerrar }: { producto: ProductoConPrecio | null; alCerrar: () => void }) {
  const { data: cambios, isPending, isError, error } = useHistorial(producto?.id ?? null);

  return (
    <Dialogo abierto={producto !== null} alCerrar={alCerrar} titulo={`Historial · ${producto?.nombre ?? ""}`}>
      {isPending && <div aria-hidden className="h-32 animate-pulse rounded-xl bg-superficie-alta" />}
      {isError && <p role="alert">No se pudo cargar el historial: {error.message}</p>}
      {cambios && cambios.length === 0 && <p className="text-suave">Este producto todavía no tuvo cambios de precio.</p>}
      {cambios && cambios.length > 0 && (
        <ol className="divide-y divide-borde">
          {cambios.map((c, i) => (
            <li key={i} className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-1 py-3">
              <div>
                <p className="numeros text-principal">
                  <span className="text-suave line-through decoration-suave/50">{formatearPrecio(c.precioAnterior)}</span>
                  <span aria-hidden className="mx-2 text-suave">
                    →
                  </span>
                  <span className="sr-only"> pasó a </span>
                  <strong className="font-semibold">{formatearPrecio(c.precioNuevo)}</strong>
                </p>
                <p className="text-xs text-suave">
                  {motivo(c)} · {c.usuario}
                </p>
              </div>
              <time dateTime={c.cambiadoEn} className="text-sm text-suave">
                {formatearFecha(c.cambiadoEn)}
              </time>
            </li>
          ))}
        </ol>
      )}
    </Dialogo>
  );
}

function motivo(c: CambioDePrecio): string {
  if (c.motivo === "MANUAL" || c.porcentaje === null) return "Cambio manual";
  return `Aumento ${c.porcentaje > 0 ? "+" : ""}${c.porcentaje}%`;
}
