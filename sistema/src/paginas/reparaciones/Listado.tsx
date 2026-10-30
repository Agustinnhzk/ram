import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router";
import { useOrdenes, type Estado, type Vista } from "../../api/reparaciones";
import { EtiquetaDeEstado } from "../../componentes/EtiquetaDeEstado";
import { IconoLupa } from "../../componentes/Iconos";
import { formatearFecha } from "../../utilidades/formato";
import { ESTADOS, formatearDia } from "../../utilidades/reparaciones";
import { useRetrasado } from "../../utilidades/useRetrasado";

const VISTAS: { valor: Vista; texto: string }[] = [
  { valor: "EN_TALLER", texto: "En el taller" },
  { valor: "ENTREGADAS", texto: "Entregadas" },
  { valor: "TODAS", texto: "Todas" },
];

/** /reparaciones — la pantalla de todos los días: qué hay en el taller y en qué está cada equipo. */
export function Listado() {
  const [vista, setVista] = useState<Vista>("EN_TALLER");
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState<Estado | null>(null);
  const { data: ordenes, isPending, isError, error } = useOrdenes(vista, useRetrasado(busqueda));
  const navegar = useNavigate();

  // Cuántas hay en cada estado (para los filtros rápidos de "En el taller").
  const porEstado = useMemo(() => {
    const cuenta = new Map<Estado, number>();
    ordenes?.forEach((o) => cuenta.set(o.estado, (cuenta.get(o.estado) ?? 0) + 1));
    return (Object.keys(ESTADOS) as Estado[]).filter((e) => cuenta.has(e)).map((e) => ({ estado: e, cantidad: cuenta.get(e)! }));
  }, [ordenes]);

  const visibles = ordenes?.filter((o) => !estado || o.estado === estado);

  function cambiarVista(nueva: Vista) {
    setVista(nueva);
    setEstado(null);
  }

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="texto-metalico text-4xl sm:text-5xl">Reparaciones</h1>
        <Link to="/reparaciones/nueva" className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
          + Nuevo ingreso
        </Link>
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-3">
        <div role="group" aria-label="Qué órdenes ver" className="flex rounded-full border border-borde bg-superficie p-1">
          {VISTAS.map((v) => (
            <button
              key={v.valor}
              type="button"
              aria-pressed={vista === v.valor}
              onClick={() => cambiarVista(v.valor)}
              className={`rounded-full px-4 py-1.5 text-sm ${vista === v.valor ? "bg-oro font-semibold text-negro" : "text-principal hover:text-oro-claro"}`}
            >
              {v.texto}
            </button>
          ))}
        </div>
        <div className="flex min-w-60 flex-1 items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
          <IconoLupa className="size-5 text-suave" />
          <label htmlFor="buscar-orden" className="sr-only">
            Buscar por número de orden o cliente
          </label>
          <input
            id="buscar-orden"
            type="search"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="N° de orden o nombre del cliente…"
            className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
          />
        </div>
      </div>

      {vista === "EN_TALLER" && porEstado.length > 1 && (
        <div className="mt-4 flex flex-wrap gap-2" role="group" aria-label="Filtrar por estado">
          <FiltroRapido activo={estado === null} alElegir={() => setEstado(null)} texto={`Todas (${ordenes?.length ?? 0})`} />
          {porEstado.map(({ estado: e, cantidad }) => (
            <FiltroRapido key={e} activo={estado === e} alElegir={() => setEstado(e)} texto={`${ESTADOS[e].nombre} (${cantidad})`} />
          ))}
        </div>
      )}

      {isPending && <div aria-hidden className="mt-6 h-80 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-6 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudieron cargar las reparaciones: {error.message}
        </p>
      )}

      {visibles && visibles.length === 0 && (
        <div className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center">
          <p className="text-principal">
            {busqueda ? "No hay órdenes que coincidan con la búsqueda." : vista === "EN_TALLER" ? "No hay equipos en el taller." : "No hay órdenes."}
          </p>
          {!busqueda && vista === "EN_TALLER" && (
            <Link to="/reparaciones/nueva" className="mt-3 inline-block text-oro underline underline-offset-4">
              Registrar un ingreso
            </Link>
          )}
        </div>
      )}

      {/* En el celular, tarjetas: una tabla de 5 columnas no entra y el estado quedaba escondido. */}
      {visibles && visibles.length > 0 && (
        <ul className="mt-6 space-y-3 sm:hidden">
          {visibles.map((o) => (
            <li key={o.id}>
              <Link to={`/reparaciones/${o.id}`} className="block rounded-xl border border-borde bg-superficie p-4 hover:border-oro">
                <div className="flex items-center justify-between gap-3">
                  <span className="numeros font-display text-xl font-bold text-oro-claro">{o.numero}</span>
                  <EtiquetaDeEstado estado={o.estado} enGarantia={o.enGarantia} />
                </div>
                <p className="mt-1 text-principal">{o.cliente}</p>
                <p className="text-sm text-suave">
                  {o.equipo} · {formatearFecha(o.ingresadoEn)}
                </p>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {visibles && visibles.length > 0 && (
        <div className="relative mt-6 hidden overflow-x-auto rounded-xl border border-borde sm:block">
          <table className="w-full min-w-[44rem] text-left text-sm">
            <thead className="bg-superficie text-xs tracking-wide text-suave uppercase">
              <tr>
                <th scope="col" className="px-4 py-3 font-medium">N°</th>
                <th scope="col" className="px-4 py-3 font-medium">Cliente</th>
                <th scope="col" className="px-4 py-3 font-medium">Equipo</th>
                <th scope="col" className="px-4 py-3 font-medium">Estado</th>
                <th scope="col" className="px-4 py-3 font-medium">{vista === "ENTREGADAS" ? "Garantía" : "Ingresó"}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-borde">
              {visibles.map((o) => (
                // Toda la fila se puede tocar (cómodo con el mouse); para teclado y lectores, el link es el número.
                <tr key={o.id} onClick={() => navegar(`/reparaciones/${o.id}`)} className="cursor-pointer hover:bg-superficie">
                  <td className="px-4 py-3">
                    <Link to={`/reparaciones/${o.id}`} className="numeros font-display text-lg font-bold text-oro-claro hover:underline">
                      {o.numero}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <span className="text-principal">{o.cliente}</span>
                    {o.telefono && <span className="numeros block text-xs text-suave">{o.telefono}</span>}
                  </td>
                  <td className="px-4 py-3 text-principal">{o.equipo}</td>
                  <td className="px-4 py-3">
                    <EtiquetaDeEstado estado={o.estado} enGarantia={o.enGarantia} />
                  </td>
                  <td className="numeros px-4 py-3 text-suave">
                    {vista === "ENTREGADAS"
                      ? o.garantiaHasta
                        ? `${o.enGarantia ? "Hasta" : "Venció"} ${formatearDia(o.garantiaHasta)}`
                        : "Sin garantía"
                      : formatearFecha(o.ingresadoEn)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

function FiltroRapido({ activo, alElegir, texto }: { activo: boolean; alElegir: () => void; texto: string }) {
  return (
    <button
      type="button"
      aria-pressed={activo}
      onClick={alElegir}
      className={`rounded-full border px-3 py-1 text-xs ${activo ? "border-oro bg-oro/15 text-oro-claro" : "border-borde text-suave hover:border-oro"}`}
    >
      {texto}
    </button>
  );
}
