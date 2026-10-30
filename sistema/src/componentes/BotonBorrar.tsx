import { useState, type ReactNode } from "react";
import { useAvisar } from "./contextoDeAvisos";
import { Dialogo } from "./Dialogo";

/**
 * Un botón de borrar con pregunta de confirmación (ADR-016). Borrar no se puede deshacer: por eso
 * siempre pregunta, y el botón de confirmar está en rojo y dice exactamente qué pasa.
 */
export function BotonBorrar(props: {
  texto: string;
  titulo: string;
  children: ReactNode;
  confirmar: string;
  alBorrar: () => Promise<unknown>;
  alTerminar: () => void;
}) {
  const [abierto, setAbierto] = useState(false);
  const [borrando, setBorrando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const avisar = useAvisar();

  async function borrar() {
    setBorrando(true);
    setError(null);
    try {
      await props.alBorrar();
      setAbierto(false);
      avisar("Borrado");
      props.alTerminar();
    } catch (e) {
      setError(e instanceof Error ? e.message : "No se pudo borrar.");
    } finally {
      setBorrando(false);
    }
  }

  return (
    <>
      <button
        type="button"
        onClick={() => setAbierto(true)}
        className="rounded-full border border-peligro/60 px-4 py-2 text-sm font-semibold text-peligro hover:bg-peligro/10"
      >
        {props.texto}
      </button>
      <Dialogo abierto={abierto} alCerrar={() => setAbierto(false)} titulo={props.titulo} ancho="max-w-md">
        <div className="space-y-4">
          <div className="text-suave">{props.children}</div>
          <p className="font-semibold text-principal">Esto no se puede deshacer.</p>
          {error && (
            <p role="alert" className="text-sm text-peligro">
              {error}
            </p>
          )}
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => setAbierto(false)} className="px-4 py-2 text-sm text-suave hover:text-principal">
              Cancelar
            </button>
            <button
              type="button"
              disabled={borrando}
              onClick={borrar}
              className="rounded-full bg-peligro px-5 py-2 text-sm font-semibold text-white hover:brightness-110 disabled:opacity-50"
            >
              {props.confirmar}
            </button>
          </div>
        </div>
      </Dialogo>
    </>
  );
}
