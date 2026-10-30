import { useCallback, useState, type ReactNode } from "react";
import { ContextoDeAvisos, type TipoDeAviso } from "./contextoDeAvisos";

/**
 * Avisos cortos abajo a la derecha ("Precio actualizado"). Se van solos a los 4 segundos.
 * aria-live="polite": un lector de pantalla los lee sin interrumpir lo que está leyendo.
 */

type Aviso = { id: number; texto: string; tipo: TipoDeAviso };

let siguienteId = 0;

export function ProveedorDeAvisos({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([]);

  const avisar = useCallback((texto: string, tipo: TipoDeAviso = "exito") => {
    const id = ++siguienteId;
    setAvisos((actuales) => [...actuales, { id, texto, tipo }]);
    setTimeout(() => setAvisos((actuales) => actuales.filter((a) => a.id !== id)), 4000);
  }, []);

  return (
    <ContextoDeAvisos.Provider value={avisar}>
      {children}
      <div aria-live="polite" className="pointer-events-none fixed right-4 bottom-4 z-50 flex flex-col items-end gap-2">
        {avisos.map((a) => (
          <p
            key={a.id}
            className={`rounded-lg border bg-superficie-alta px-4 py-3 text-sm shadow-lg ${
              a.tipo === "exito" ? "border-exito/60 text-principal" : "border-peligro/70 text-principal"
            }`}
          >
            <span aria-hidden className={a.tipo === "exito" ? "mr-2 text-exito" : "mr-2 text-peligro"}>
              {a.tipo === "exito" ? "✓" : "!"}
            </span>
            {a.texto}
          </p>
        ))}
      </div>
    </ContextoDeAvisos.Provider>
  );
}
