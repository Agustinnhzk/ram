import { Link, useNavigate } from "react-router";
import { useState } from "react";
import { usePedidos, type EstadoPedido } from "../../api/pedidos";
import { EtiquetaDePedido } from "../../componentes/EtiquetaDePedido";
import { formatearFecha, formatearPrecio } from "../../utilidades/formato";
import { ENTREGAS } from "../../utilidades/pedidos";

const FILTROS: { valor: EstadoPedido | null; texto: string }[] = [
  { valor: null, texto: "Activos" },
  { valor: "PAGO_INFORMADO", texto: "Revisar pago" },
  { valor: "ESPERANDO_PAGO", texto: "Esperando pago" },
  { valor: "PAGADO", texto: "A entregar" },
  { valor: "ENTREGADO", texto: "Entregados" },
  { valor: "CANCELADO", texto: "Cancelados" },
];

/** /pedidos — las compras de la tienda. Lo primero a mirar: los que tienen un comprobante para revisar. */
export function Listado() {
  const [filtro, setFiltro] = useState<EstadoPedido | null>(null);
  const { data: pedidos, isPending, isError, error } = usePedidos(filtro);
  const navegar = useNavigate();
  const paraRevisar = filtro === null ? (pedidos?.filter((p) => p.estado === "PAGO_INFORMADO").length ?? 0) : 0;

  return (
    <>
      <h1 className="texto-metalico text-4xl sm:text-5xl">Pedidos</h1>
      <p className="mt-1 text-suave">Las compras de la tienda, con pago por transferencia.</p>

      <div className="mt-6 flex flex-wrap gap-2" role="group" aria-label="Qué pedidos ver">
        {FILTROS.map((f) => (
          <button
            key={f.texto}
            type="button"
            aria-pressed={filtro === f.valor}
            onClick={() => setFiltro(f.valor)}
            className={`rounded-full border px-4 py-1.5 text-sm ${
              filtro === f.valor ? "border-oro bg-oro font-semibold text-negro" : "border-borde text-principal hover:border-oro"
            }`}
          >
            {f.texto}
          </button>
        ))}
      </div>

      {paraRevisar > 0 && (
        <p role="status" className="mt-4 rounded-xl border border-info/50 bg-info/10 px-4 py-3 text-sm">
          Hay <strong>{paraRevisar}</strong> {paraRevisar === 1 ? "pedido" : "pedidos"} con comprobante para revisar.
        </p>
      )}

      {isPending && <div aria-hidden className="mt-6 h-80 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-6 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudieron cargar los pedidos: {error.message}
        </p>
      )}
      {pedidos && pedidos.length === 0 && (
        <p className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center text-principal">
          {filtro === null ? "No hay pedidos pendientes. 🎉" : "No hay pedidos en este estado."}
        </p>
      )}

      {pedidos && pedidos.length > 0 && (
        <>
          <ul className="mt-6 space-y-3 sm:hidden">
            {pedidos.map((p) => (
              <li key={p.id}>
                <Link to={`/pedidos/${p.id}`} className="block rounded-xl border border-borde bg-superficie p-4 hover:border-oro">
                  <div className="flex items-center justify-between gap-3">
                    <span className="numeros font-display text-xl font-bold text-oro-claro">{p.numero}</span>
                    <EtiquetaDePedido estado={p.estado} />
                  </div>
                  <p className="mt-1 text-principal">{p.cliente}</p>
                  <p className="text-sm text-suave">
                    {formatearPrecio(p.total)} · {formatearFecha(p.creadoEn)}
                  </p>
                </Link>
              </li>
            ))}
          </ul>

          <div className="relative mt-6 hidden overflow-x-auto rounded-xl border border-borde sm:block">
            <table className="w-full min-w-[44rem] text-left text-sm">
              <thead className="bg-superficie text-xs tracking-wide text-suave uppercase">
                <tr>
                  <th scope="col" className="px-4 py-3 font-medium">N°</th>
                  <th scope="col" className="px-4 py-3 font-medium">Cliente</th>
                  <th scope="col" className="px-4 py-3 font-medium">Entrega</th>
                  <th scope="col" className="px-4 py-3 text-right font-medium">Total</th>
                  <th scope="col" className="px-4 py-3 font-medium">Estado</th>
                  <th scope="col" className="px-4 py-3 font-medium">Fecha</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-borde">
                {pedidos.map((p) => (
                  <tr key={p.id} onClick={() => navegar(`/pedidos/${p.id}`)} className="cursor-pointer hover:bg-superficie">
                    <td className="px-4 py-3">
                      <Link to={`/pedidos/${p.id}`} className="numeros font-display text-lg font-bold text-oro-claro hover:underline">
                        {p.numero}
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-principal">{p.cliente}</span>
                      <span className="numeros block text-xs text-suave">{p.telefono}</span>
                    </td>
                    <td className="px-4 py-3 text-suave">{ENTREGAS[p.entrega]}</td>
                    <td className="numeros px-4 py-3 text-right text-principal">{formatearPrecio(p.total)}</td>
                    <td className="px-4 py-3">
                      <EtiquetaDePedido estado={p.estado} />
                    </td>
                    <td className="numeros px-4 py-3 text-suave">{formatearFecha(p.creadoEn)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  );
}
