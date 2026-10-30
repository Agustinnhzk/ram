import { useState, type FormEvent, type KeyboardEvent } from "react";
import { useCambiarPrecio, type ProductoConPrecio } from "../../api/precios";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { IconoReloj } from "../../componentes/Iconos";
import { formatearPrecio, leerPrecio } from "../../utilidades/formato";

const miles = new Intl.NumberFormat("es-AR", { maximumFractionDigits: 0 });

/**
 * Una fila de la tabla de precios. El dueño escribe directo en el campo, como en una planilla:
 * Enter guarda, Esc vuelve al precio anterior. Vacío = "a consultar".
 */
export function FilaDePrecio(props: { producto: ProductoConPrecio; editable: boolean; alVerHistorial: () => void }) {
  const { producto } = props;
  const original = producto.precio === null ? "" : miles.format(producto.precio);
  const [texto, setTexto] = useState(original);
  const cambiar = useCambiarPrecio();
  const avisar = useAvisar();

  const vacio = texto.trim() === "";
  const nuevo = vacio ? null : leerPrecio(texto);
  const invalido = !vacio && nuevo === null;
  const modificado = !invalido && nuevo !== producto.precio;

  function guardar(evento?: FormEvent) {
    evento?.preventDefault();
    if (!modificado || cambiar.isPending) return;
    cambiar.mutate(
      { id: producto.id, precio: nuevo },
      {
        onSuccess: () => avisar(`${producto.nombre}: ${formatearPrecio(nuevo)}`),
        onError: (e) => avisar(`No se pudo guardar el precio de ${producto.nombre}: ${e.message}`, "error"),
      },
    );
  }

  function teclas(evento: KeyboardEvent<HTMLInputElement>) {
    if (evento.key === "Escape") setTexto(original);
  }

  return (
    <tr className={modificado ? "bg-oro/5" : undefined}>
      <td className="px-4 py-3">
        <span className="text-principal">{producto.nombre}</span>
        {producto.esComponente && <Etiqueta>Armá tu PC</Etiqueta>}
        {!producto.publicado && !producto.esComponente && <Etiqueta>No publicado</Etiqueta>}
      </td>

      <td className="px-4 py-2">
        {props.editable ? (
          <form onSubmit={guardar} className="flex items-center gap-2">
            <div
              className={`flex w-36 items-center rounded-lg border bg-fondo focus-within:border-oro ${
                invalido ? "border-peligro" : modificado ? "border-oro" : "border-borde"
              }`}
            >
              <span className="pl-2.5 text-suave" aria-hidden>
                $
              </span>
              <input
                value={texto}
                onChange={(e) => setTexto(e.target.value)}
                onKeyDown={teclas}
                inputMode="numeric"
                placeholder="A consultar"
                aria-label={`Precio de ${producto.nombre}`}
                aria-invalid={invalido}
                aria-describedby={invalido ? `error-${producto.id}` : undefined}
                className="numeros w-full bg-transparent px-2 py-1.5 text-right text-principal placeholder:text-suave/60 focus:outline-none"
              />
            </div>
            {modificado && (
              <button
                type="submit"
                disabled={cambiar.isPending}
                className="rounded-full bg-oro px-3 py-1 text-xs font-semibold text-negro hover:brightness-110 disabled:opacity-60"
              >
                {cambiar.isPending ? "…" : vacio ? "A consultar" : "Guardar"}
              </button>
            )}
            {invalido && (
              <span id={`error-${producto.id}`} className="text-xs text-peligro">
                Solo números
              </span>
            )}
          </form>
        ) : (
          <span className={`numeros ${producto.precio === null ? "text-suave" : "text-principal"}`}>
            {formatearPrecio(producto.precio)}
          </span>
        )}
      </td>

      <td className="px-4 py-3 text-right">
        <button
          type="button"
          onClick={props.alVerHistorial}
          className="inline-flex items-center gap-1.5 text-xs text-suave hover:text-oro-claro"
          aria-label={`Historial de precios de ${producto.nombre}`}
        >
          <IconoReloj className="size-4" />
          Historial
        </button>
      </td>
    </tr>
  );
}

function Etiqueta({ children }: { children: string }) {
  return <span className="ml-2 rounded-full border border-borde px-2 py-0.5 text-[0.7rem] text-suave">{children}</span>;
}
