"use client";

import { useId, useState, type InputHTMLAttributes, type ReactNode, type TextareaHTMLAttributes } from "react";

/** Piezas de formulario de la tienda: mismas medidas, foco y errores en todos lados. */

const estiloDeCampo =
  "mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal placeholder:text-suave/60 focus:border-oro focus:outline-none aria-[invalid=true]:border-red-500";

type PropsDeCampo = InputHTMLAttributes<HTMLInputElement> & { etiqueta: string; ayuda?: ReactNode; error?: string };

export function Campo({ etiqueta, ayuda, error, className = "", ...input }: PropsDeCampo) {
  const id = useId();
  return (
    <div className={className}>
      <label htmlFor={id} className="text-sm text-suave">
        {etiqueta}
      </label>
      <input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={ayuda || error ? `${id}-ayuda` : undefined}
        className={estiloDeCampo}
        {...input}
      />
      {(error || ayuda) && (
        <p id={`${id}-ayuda`} className={`mt-1 text-xs ${error ? "text-red-500" : "text-suave"}`}>
          {error ?? ayuda}
        </p>
      )}
    </div>
  );
}

/** Contraseña con botón para verla: en el celular es fácil equivocarse y no enterarse. */
export function CampoDeClave(props: Omit<PropsDeCampo, "type">) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="relative">
      <Campo {...props} type={visible ? "text" : "password"} />
      <button
        type="button"
        onClick={() => setVisible(!visible)}
        className="absolute top-7 right-2 px-2 py-1.5 text-xs text-oro hover:text-oro-claro"
        aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
      >
        {visible ? "Ocultar" : "Ver"}
      </button>
    </div>
  );
}

export function AreaDeTexto({
  etiqueta,
  ayuda,
  className = "",
  ...area
}: TextareaHTMLAttributes<HTMLTextAreaElement> & { etiqueta: string; ayuda?: string }) {
  const id = useId();
  return (
    <div className={className}>
      <label htmlFor={id} className="text-sm text-suave">
        {etiqueta}
      </label>
      <textarea id={id} rows={4} className={`${estiloDeCampo} resize-y`} {...area} />
      {ayuda && <p className="mt-1 text-xs text-suave">{ayuda}</p>}
    </div>
  );
}

/** Un mensaje para la persona: error (rojo) o éxito (dorado). role="alert" lo lee el lector de pantalla al aparecer. */
export function Aviso({ tipo, children }: { tipo: "error" | "exito" | "info"; children: ReactNode }) {
  const estilos = {
    error: "border-red-500/60 bg-red-500/10",
    exito: "border-oro/60 bg-oro/10",
    info: "border-borde bg-superficie",
  };
  return (
    <div role={tipo === "error" ? "alert" : "status"} className={`rounded-xl border p-4 text-sm text-principal ${estilos[tipo]}`}>
      {children}
    </div>
  );
}

export function BotonPrincipal({
  enviando,
  children,
  className = "",
  ...boton
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { enviando?: boolean }) {
  return (
    <button
      type="submit"
      disabled={enviando || boton.disabled}
      className={`boton-oro rounded-full px-6 py-3 font-semibold hover:brightness-110 disabled:cursor-wait disabled:opacity-60 ${className}`}
      {...boton}
    >
      {enviando ? "Un momento…" : children}
    </button>
  );
}

/**
 * Campo trampa para robots (ver ADR-010): una persona no lo ve ni lo puede enfocar; los robots que completan
 * todos los campos sí lo llenan, y el servidor descarta el envío.
 */
export function CampoTrampa() {
  return (
    <div aria-hidden className="absolute -left-[9999px] h-px w-px overflow-hidden">
      <label>
        Sitio web
        <input name="sitioWeb" type="text" tabIndex={-1} autoComplete="off" />
      </label>
    </div>
  );
}
