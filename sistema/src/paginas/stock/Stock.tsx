import { useMemo, useState } from "react";
import { Link } from "react-router";
import { useSesion } from "../../api/sesion";
import { useCambiarStock, useReservasPendientes, useStock, type FilaDeStock } from "../../api/stock";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { IconoLupa } from "../../componentes/Iconos";
import { formatearFecha } from "../../utilidades/formato";
import { NOMBRE_DEL_TIPO } from "../../utilidades/productos";
import { ESTILO_DEL_ESTADO } from "../../utilidades/stock";
import { normalizar } from "../../utilidades/texto";
import { DialogoDeStock } from "./DialogoDeStock";

type Vista = "todos" | "controlados" | "sin-stock" | "pocas" | "sin-control";

const VISTAS: { valor: Vista; texto: string }[] = [
  { valor: "todos", texto: "Todos" },
  { valor: "controlados", texto: "Con control" },
  { valor: "sin-stock", texto: "Sin stock" },
  { valor: "pocas", texto: "Pocas unidades" },
  { valor: "sin-control", texto: "Sin control" },
];

/**
 * /stock — cuánto hay de cada producto, las reservas que esperan mercadería y el historial (ADR-014).
 * Un producto sin control se vende sin límite, como siempre; con control, la tienda muestra si hay.
 */
export function Stock() {
  const { data: sesion } = useSesion();
  const { data: filas, isPending, isError, error } = useStock();
  const [busqueda, setBusqueda] = useState("");
  const [vista, setVista] = useState<Vista>("todos");
  const [elegido, setElegido] = useState<string | null>(null);
  const manejaStock = sesion?.rol === "DUENO" || sesion?.rol === "MOSTRADOR";

  const visibles = useMemo(() => {
    const palabras = normalizar(busqueda).split(/\s+/).filter(Boolean);
    return (filas ?? []).filter((f) => {
      if (vista === "controlados" && f.cantidad === null) return false;
      if (vista === "sin-stock" && f.estado !== "SIN_STOCK") return false;
      if (vista === "pocas" && f.estado !== "ULTIMAS_UNIDADES") return false;
      if (vista === "sin-control" && f.cantidad !== null) return false;
      const texto = normalizar(`${f.nombre} ${f.categoria}`);
      return palabras.every((p) => texto.includes(p));
    });
  }, [filas, busqueda, vista]);

  const fila = filas?.find((f) => f.id === elegido) ?? null;

  return (
    <>
      <h1 className="texto-metalico text-4xl sm:text-5xl">Stock</h1>
      <p className="mt-2 text-suave">
        Se descuenta solo cuando confirmás el pago de un pedido. Lo que se vende sin stock queda reservado.
        {!manejaStock && " Lo cargan el Dueño y Mostrador."}
      </p>

      <ReservasPendientes manejaStock={manejaStock} />

      <h2 className="mt-10 text-2xl">Productos</h2>
      <div className="mt-3 flex items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
        <IconoLupa className="size-5 text-suave" />
        <label htmlFor="buscar-stock" className="sr-only">
          Buscar producto
        </label>
        <input
          id="buscar-stock"
          type="search"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          placeholder="Buscar por nombre o categoría…"
          className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
        />
      </div>
      <div className="mt-3 flex flex-wrap gap-2" role="group" aria-label="Qué productos ver">
        {VISTAS.map((v) => (
          <button
            key={v.valor}
            type="button"
            aria-pressed={vista === v.valor}
            onClick={() => setVista(v.valor)}
            className={`rounded-full border px-3 py-1 text-xs ${vista === v.valor ? "border-oro bg-oro/15 text-oro-claro" : "border-borde text-suave hover:border-oro"}`}
          >
            {v.texto}
          </button>
        ))}
      </div>

      {isPending && <div aria-hidden className="mt-4 h-80 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-4 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudo cargar el stock: {error.message}
        </p>
      )}
      {filas && visibles.length === 0 && (
        <p className="mt-4 rounded-xl border border-dashed border-borde px-6 py-12 text-center">No hay productos que coincidan.</p>
      )}
      {visibles.length > 0 && (
        <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde">
          {visibles.map((f) => (
            <li key={f.id}>
              <button type="button" onClick={() => setElegido(f.id)} className="flex w-full items-center gap-4 px-4 py-3 text-left hover:bg-superficie">
                <span className="min-w-0 flex-1">
                  <span className="block truncate font-semibold text-principal">{f.nombre}</span>
                  <span className="flex flex-wrap gap-x-3 text-xs text-suave">
                    <span>{f.categoria}</span>
                    {f.tipoComponente && <span>· {NOMBRE_DEL_TIPO[f.tipoComponente]}</span>}
                    {!f.publicado && !f.tipoComponente && <span>· Oculto en la tienda</span>}
                    {f.reservado > 0 && <span className="text-aviso">· {f.reservado} {f.reservado === 1 ? "reservada" : "reservadas"}</span>}
                  </span>
                </span>
                <CantidadDeStock fila={f} />
              </button>
            </li>
          ))}
        </ul>
      )}

      <DialogoDeStock fila={fila} manejaStock={manejaStock} esDueno={sesion?.rol === "DUENO"} alCerrar={() => setElegido(null)} />
    </>
  );
}

function CantidadDeStock({ fila }: { fila: FilaDeStock }) {
  if (fila.cantidad === null || fila.estado === null) {
    return <span className="shrink-0 text-xs text-suave">Sin control</span>;
  }
  const { texto, clases } = ESTILO_DEL_ESTADO[fila.estado];
  return (
    <span className="flex shrink-0 items-center gap-2">
      <span className={`hidden rounded-full border px-2 py-0.5 text-xs sm:inline ${clases}`}>{texto}</span>
      <span className="numeros w-12 text-right text-xl font-semibold text-principal">{fila.cantidad}</span>
    </span>
  );
}

/** Lo pagado que no había: el taller elige a qué pedido le da las unidades cuando llegan. */
function ReservasPendientes({ manejaStock }: { manejaStock: boolean }) {
  const { data: reservas } = useReservasPendientes();
  const cambiar = useCambiarStock();
  const avisar = useAvisar();

  if (!reservas || reservas.length === 0) return null;

  return (
    <section className="mt-8 rounded-xl border border-aviso/50 bg-aviso/5 p-5">
      <h2 className="text-2xl">Reservas que esperan mercadería</h2>
      <p className="mt-1 text-sm text-suave">
        Pedidos pagados sin stock. Cuando llegue, cargala y tocá “Separar” en el pedido que quieras atender primero.
      </p>
      <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde bg-fondo">
        {reservas.map((r) => (
          <li key={r.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
            <span className="min-w-0">
              <span className="block font-semibold text-principal">
                <span className="numeros">{r.cantidad}</span> × {r.productoNombre}
              </span>
              <span className="text-xs text-suave">
                <Link to={`/pedidos/${r.pedido}`} className="text-oro-claro underline-offset-4 hover:underline">
                  Pedido <span className="numeros">{r.numeroPedido}</span>
                </Link>{" "}
                · pagado el <span className="numeros">{formatearFecha(r.creadaEn)}</span>
              </span>
            </span>
            {manejaStock &&
              (r.sePuedeCubrir ? (
                <button
                  type="button"
                  disabled={cambiar.isPending}
                  onClick={() =>
                    cambiar.mutate(
                      { tipo: "cubrir", reserva: r.id },
                      { onSuccess: () => avisar(`Separado para el pedido ${r.numeroPedido}`), onError: (e) => avisar(e.message, "error") },
                    )
                  }
                  className="boton-oro rounded-full px-4 py-1.5 text-sm font-semibold disabled:opacity-50"
                >
                  Separar
                </button>
              ) : (
                <span className="text-xs text-suave">Falta mercadería</span>
              ))}
          </li>
        ))}
      </ul>
    </section>
  );
}
