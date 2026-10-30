import { useState, type FormEvent } from "react";
import { useAplicarAumento, useVistaPrevia, type Aumento } from "../../api/precios";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";
import { formatearPrecio, leerPorcentaje } from "../../utilidades/formato";

type Props = {
  abierto: boolean;
  alCerrar: () => void;
  categorias: { slug: string; nombre: string }[];
  puedeAplicar: boolean;
};

/**
 * Aumento (o rebaja) por porcentaje, en dos pasos:
 * 1. Elegís el porcentaje y la categoría, y ves cómo quedarían los precios (no cambia nada).
 * 2. Si está bien, lo aplicás. Todo o nada: el backend lo hace en una sola transacción (ADR-008).
 */
export function DialogoAumento(props: Props) {
  return (
    <Dialogo abierto={props.abierto} alCerrar={props.alCerrar} titulo="Aumento por porcentaje" ancho="max-w-3xl">
      {/* Adentro del diálogo, para que al cerrarlo se borre todo y la próxima vez arranque de cero. */}
      <Contenido {...props} />
    </Dialogo>
  );
}

function Contenido({ alCerrar, categorias, puedeAplicar }: Props) {
  const [porcentajeTexto, setPorcentajeTexto] = useState("");
  const [categoria, setCategoria] = useState("");
  const vistaPrevia = useVistaPrevia();
  const aplicar = useAplicarAumento();
  const avisar = useAvisar();

  const porcentaje = leerPorcentaje(porcentajeTexto);
  const pedido: Aumento | null = porcentaje === null ? null : { porcentaje, categoria: categoria || null };
  const resultado = vistaPrevia.data;

  function verVistaPrevia(evento: FormEvent) {
    evento.preventDefault();
    if (pedido) vistaPrevia.mutate(pedido);
  }

  function confirmar() {
    if (!resultado) return;
    aplicar.mutate(
      { porcentaje: resultado.porcentaje, categoria: resultado.categoria },
      {
        onSuccess: ({ cambiados }) => {
          avisar(cambiados === 1 ? "Se actualizó 1 precio" : `Se actualizaron ${cambiados} precios`);
          alCerrar();
        },
      },
    );
  }

  // Paso 2: la vista previa.
  if (resultado) {
    const signo = resultado.porcentaje > 0 ? "+" : "";
    const nombreCategoria = categorias.find((c) => c.slug === resultado.categoria)?.nombre ?? "Todas las categorías";
    return (
      <>
        <p className="text-principal">
          <strong className="text-oro-claro">
            {signo}
            {resultado.porcentaje}%
          </strong>{" "}
          a {nombreCategoria.toLowerCase()} · {resultado.precios.length} productos con precio. Los que están “a consultar” no se tocan.
        </p>

        {resultado.precios.length === 0 ? (
          <p className="mt-6 text-suave">No hay productos con precio en esa categoría.</p>
        ) : (
          <div className="mt-4 max-h-[45dvh] overflow-y-auto rounded-xl border border-borde">
            <table className="w-full text-left text-sm">
              <thead className="sticky top-0 bg-superficie-alta text-xs tracking-wide text-suave uppercase">
                <tr>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Producto
                  </th>
                  <th scope="col" className="px-4 py-2 text-right font-medium">
                    Ahora
                  </th>
                  <th scope="col" className="px-4 py-2 text-right font-medium">
                    Queda
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-borde">
                {resultado.precios.map((p) => (
                  <tr key={p.id}>
                    <td className="px-4 py-2">
                      {p.nombre} <span className="text-xs text-suave">· {p.categoria}</span>
                    </td>
                    <td className="numeros px-4 py-2 text-right text-suave">{formatearPrecio(p.precioActual)}</td>
                    <td className="numeros px-4 py-2 text-right font-semibold text-principal">{formatearPrecio(p.precioNuevo)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {aplicar.isError && (
          <p role="alert" className="mt-4 rounded-lg border border-peligro/60 bg-peligro/10 px-3 py-2 text-sm">
            No se pudo aplicar: {aplicar.error.message}
          </p>
        )}

        <div className="mt-6 flex flex-wrap items-center justify-end gap-3">
          <button type="button" onClick={() => vistaPrevia.reset()} className="rounded-full border border-borde px-5 py-2 hover:border-oro">
            Cambiar porcentaje
          </button>
          {puedeAplicar ? (
            <button
              type="button"
              onClick={confirmar}
              disabled={aplicar.isPending || resultado.precios.length === 0}
              className="boton-oro rounded-full px-5 py-2 font-semibold hover:brightness-110 disabled:opacity-50"
            >
              {aplicar.isPending ? "Aplicando…" : `Aplicar a ${resultado.precios.length} productos`}
            </button>
          ) : (
            <p className="text-sm text-suave">Solo el dueño puede aplicar aumentos.</p>
          )}
        </div>
      </>
    );
  }

  // Paso 1: el formulario.
  return (
    <form onSubmit={verVistaPrevia} noValidate>
      <p className="text-suave">Para una rebaja, poné el porcentaje en negativo (por ejemplo, -10). Los precios se redondean al peso.</p>
      <div className="mt-5 grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor="porcentaje" className="text-sm text-suave">
            Porcentaje
          </label>
          <div className="mt-1 flex items-center rounded-lg border border-borde bg-fondo focus-within:border-oro">
            <input
              id="porcentaje"
              value={porcentajeTexto}
              onChange={(e) => setPorcentajeTexto(e.target.value)}
              inputMode="decimal"
              placeholder="8"
              autoFocus
              aria-invalid={porcentajeTexto !== "" && porcentaje === null}
              className="numeros w-full bg-transparent px-3 py-2.5 text-principal focus:outline-none"
            />
            <span className="pr-3 text-suave" aria-hidden>
              %
            </span>
          </div>
          {porcentajeTexto !== "" && porcentaje === null && (
            <p className="mt-1 text-xs text-peligro">Un número distinto de 0, con hasta 2 decimales.</p>
          )}
        </div>
        <div>
          <label htmlFor="categoria-aumento" className="text-sm text-suave">
            A qué productos
          </label>
          <select
            id="categoria-aumento"
            value={categoria}
            onChange={(e) => setCategoria(e.target.value)}
            className="mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal focus:border-oro focus:outline-none"
          >
            <option value="">Todas las categorías</option>
            {categorias.map((c) => (
              <option key={c.slug} value={c.slug}>
                {c.nombre}
              </option>
            ))}
          </select>
        </div>
      </div>

      {vistaPrevia.isError && (
        <p role="alert" className="mt-4 rounded-lg border border-peligro/60 bg-peligro/10 px-3 py-2 text-sm">
          {vistaPrevia.error.message}
        </p>
      )}

      <div className="mt-6 flex justify-end">
        <button
          type="submit"
          disabled={!pedido || vistaPrevia.isPending}
          className="boton-oro rounded-full px-5 py-2 font-semibold hover:brightness-110 disabled:opacity-50"
        >
          {vistaPrevia.isPending ? "Calculando…" : "Ver cómo quedan"}
        </button>
      </div>
    </form>
  );
}
