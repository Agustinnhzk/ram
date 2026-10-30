import { useState } from "react";
import { Link } from "react-router";
import { ESTADOS_DE_INSTALACION, useInstalaciones } from "../../api/instalaciones";
import { formatearDia } from "../../utilidades/reparaciones";

/** /instalaciones — un registro simple de visitas, presupuestos e instalaciones (ADR-015). */
export function Listado() {
  const [terminadas, setTerminadas] = useState(false);
  const { data: instalaciones, isPending, isError, error } = useInstalaciones(terminadas);

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Instalaciones</h1>
          <p className="mt-2 text-suave">Visitas, presupuestos e instalaciones de cámaras y alarmas.</p>
        </div>
        <Link to="/instalaciones/nueva" className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
          + Nueva instalación
        </Link>
      </div>

      <div className="mt-6 flex gap-2" role="group" aria-label="Qué instalaciones ver">
        {[false, true].map((t) => (
          <button
            key={String(t)}
            type="button"
            aria-pressed={terminadas === t}
            onClick={() => setTerminadas(t)}
            className={`rounded-full border px-3 py-1 text-xs ${terminadas === t ? "border-oro bg-oro/15 text-oro-claro" : "border-borde text-suave hover:border-oro"}`}
          >
            {t ? "Terminadas" : "En curso"}
          </button>
        ))}
      </div>

      {isPending && <div aria-hidden className="mt-4 h-60 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-4 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudieron cargar: {error.message}
        </p>
      )}
      {instalaciones && instalaciones.length === 0 && (
        <p className="mt-4 rounded-xl border border-dashed border-borde px-6 py-12 text-center">No hay instalaciones {terminadas ? "terminadas" : "en curso"}.</p>
      )}
      {instalaciones && instalaciones.length > 0 && (
        <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde">
          {instalaciones.map((i) => (
            <li key={i.id}>
              <Link to={`/instalaciones/${i.id}`} className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1 px-4 py-3 hover:bg-superficie">
                <span className="min-w-0">
                  <span className="block font-semibold text-principal">
                    <span className="numeros">{i.numero}</span> · {i.cliente}
                  </span>
                  <span className="text-sm text-suave">
                    {i.direccion}
                    {i.fecha && <span className="numeros"> · {formatearDia(i.fecha)}</span>}
                    {i.tecnico && ` · ${i.tecnico}`}
                  </span>
                </span>
                <span className="rounded-full border border-oro/50 px-2.5 py-0.5 text-xs text-oro-claro">{ESTADOS_DE_INSTALACION[i.estado]}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
