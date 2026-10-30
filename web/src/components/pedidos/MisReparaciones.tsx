"use client";

import { useEffect, useState } from "react";
import { formatearPrecio } from "@/lib/formato";
import { ESTADOS_PARA_EL_CLIENTE, misReparaciones, type Seguimiento } from "@/lib/seguimiento";

const dia = new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeZone: "America/Argentina/Buenos_Aires" });

/**
 * Las reparaciones del cliente, si su cuenta está vinculada a su ficha del taller (ADR-014).
 * Si no tiene ninguna, no se muestra nada: la mayoría de quienes compran nunca trajeron un equipo.
 */
export function MisReparaciones() {
  const [reparaciones, setReparaciones] = useState<Seguimiento[] | null>(null);

  useEffect(() => {
    // Si falla, no se muestra la sección: los pedidos siguen siendo lo principal de esta página.
    misReparaciones()
      .then(setReparaciones)
      .catch(() => setReparaciones([]));
  }, []);

  if (!reparaciones || reparaciones.length === 0) return null;

  return (
    <section aria-labelledby="mis-reparaciones" className="mt-14">
      <h2 id="mis-reparaciones" className="text-3xl text-principal">
        Mis reparaciones
      </h2>
      <ul className="mt-6 space-y-4">
        {reparaciones.map((r) => {
          const estado = ESTADOS_PARA_EL_CLIENTE[r.estado];
          const ultimoAviso = [...r.pasos].reverse().find((p) => p.nota);
          return (
            <li key={r.numero} className="rounded-xl border border-borde bg-superficie p-5">
              <div className="flex flex-wrap items-baseline justify-between gap-2">
                <p className="font-display text-xl font-bold text-principal">{r.equipo}</p>
                <p className="text-sm text-suave">
                  Orden N° {r.numero} · {dia.format(new Date(r.ingresadoEn))}
                </p>
              </div>
              <p className="mt-3 font-semibold text-oro">{r.estado === "ENTREGADO" && r.enGarantia ? "En garantía" : estado.titulo}</p>
              <p className="text-sm text-suave">{estado.detalle}</p>
              {r.estado === "PRESUPUESTADO" && r.presupuestoMonto !== null && (
                <p className="mt-2 text-principal">
                  Presupuesto: {r.presupuestoDescripcion} — <strong>{formatearPrecio(r.presupuestoMonto)}</strong>
                </p>
              )}
              {r.garantiaHasta && r.enGarantia && (
                <p className="mt-2 text-sm text-principal">Garantía hasta el {dia.format(new Date(`${r.garantiaHasta}T12:00:00`))}</p>
              )}
              {ultimoAviso?.nota && <p className="mt-2 rounded-lg bg-fondo/60 px-3 py-2 text-sm text-principal">“{ultimoAviso.nota}”</p>}
            </li>
          );
        })}
      </ul>
    </section>
  );
}
