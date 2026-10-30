import type { ReactNode } from "react";

/** Un campo de formulario con su etiqueta y, opcionalmente, una ayuda abajo. */
export function Campo(props: { id: string; etiqueta: string; ayuda?: ReactNode; opcional?: boolean; children: ReactNode }) {
  return (
    <div>
      <label htmlFor={props.id} className="text-sm text-suave">
        {props.etiqueta}
        {props.opcional && <span className="text-suave/70"> (opcional)</span>}
      </label>
      <div className="mt-1">{props.children}</div>
      {props.ayuda && (
        <p id={`${props.id}-ayuda`} className="mt-1 text-xs text-suave">
          {props.ayuda}
        </p>
      )}
    </div>
  );
}

/** Las clases de todos los inputs y textareas, para que se vean iguales en todo el sistema. */
export const ESTILO_DE_CAMPO =
  "w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal placeholder:text-suave/60 focus:border-oro focus:outline-none";
