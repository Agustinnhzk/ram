import { createContext, useContext } from "react";

export type TipoDeAviso = "exito" | "error";

/** Separado de Avisos.tsx: Vite solo recarga "en caliente" archivos que exportan únicamente componentes. */
export const ContextoDeAvisos = createContext<(texto: string, tipo?: TipoDeAviso) => void>(() => {});

/** Para mostrar un aviso desde cualquier pantalla: const avisar = useAvisar(); avisar("Listo"). */
export function useAvisar() {
  return useContext(ContextoDeAvisos);
}
