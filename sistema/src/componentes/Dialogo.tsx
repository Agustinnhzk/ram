import { useEffect, useRef, type ReactNode } from "react";
import { IconoCerrar } from "./Iconos";

/**
 * Una ventana encima de la página, con el <dialog> nativo del navegador. Gratis trae:
 * el foco queda adentro, Esc la cierra, y lo de atrás no se puede tocar. Mucho mejor que armarlo a mano.
 */
export function Dialogo(props: {
  abierto: boolean;
  alCerrar: () => void;
  titulo: string;
  children: ReactNode;
  ancho?: string;
}) {
  const { abierto, alCerrar } = props;
  const ref = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialogo = ref.current;
    if (!dialogo) return;
    if (abierto && !dialogo.open) dialogo.showModal();
    if (!abierto && dialogo.open) dialogo.close();
  }, [abierto]);

  return (
    <dialog
      ref={ref}
      onClose={alCerrar}
      aria-labelledby="titulo-dialogo"
      className={`m-auto w-[calc(100%-2rem)] ${props.ancho ?? "max-w-2xl"} rounded-2xl border border-borde bg-superficie p-0 text-principal shadow-2xl`}
    >
      {abierto && (
        <div className="flex max-h-[85dvh] flex-col">
          <div className="flex items-center justify-between gap-4 border-b border-borde px-6 py-4">
            <h2 id="titulo-dialogo" className="text-2xl">
              {props.titulo}
            </h2>
            <button
              type="button"
              onClick={alCerrar}
              className="grid size-9 place-items-center rounded-full text-suave hover:bg-superficie-alta hover:text-principal"
              aria-label="Cerrar"
            >
              <IconoCerrar className="size-5" />
            </button>
          </div>
          <div className="overflow-y-auto px-6 py-5">{props.children}</div>
        </div>
      )}
    </dialog>
  );
}
