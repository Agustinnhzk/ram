import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";

/** Las formas del JSON de /api/sistema/precios (ver PreciosController en el backend). */

export type ProductoConPrecio = {
  id: string;
  slug: string;
  nombre: string;
  categoria: { slug: string; nombre: string };
  /** null = "a consultar" */
  precio: number | null;
  publicado: boolean;
  esComponente: boolean;
};

export type PrecioPropuesto = {
  id: string;
  nombre: string;
  categoria: string;
  precioActual: number;
  precioNuevo: number;
};

export type VistaPrevia = {
  porcentaje: number;
  categoria: string | null;
  precios: PrecioPropuesto[];
};

export type CambioDePrecio = {
  precioAnterior: number | null;
  precioNuevo: number | null;
  motivo: "MANUAL" | "PORCENTAJE";
  porcentaje: number | null;
  usuario: string;
  /** ISO 8601 en UTC: "2026-10-29T17:30:00Z" */
  cambiadoEn: string;
};

export type Aumento = { porcentaje: number; categoria: string | null };

const PRECIOS = ["precios"] as const;

export function usePrecios() {
  return useQuery({
    queryKey: PRECIOS,
    queryFn: () => pedir<ProductoConPrecio[]>("GET", "/api/sistema/precios"),
  });
}

/** El historial se pide recién cuando se abre (enabled), no por cada producto de la tabla. */
export function useHistorial(id: string | null) {
  return useQuery({
    queryKey: ["precios", id, "historial"],
    queryFn: () => pedir<CambioDePrecio[]>("GET", `/api/sistema/precios/${id}/historial`),
    enabled: id !== null,
  });
}

/**
 * Cambiar un precio. Al terminar se "invalidan" los precios: TanStack Query los vuelve a pedir,
 * así la tabla y el historial muestran lo que quedó guardado de verdad en el servidor.
 */
export function useCambiarPrecio() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: ({ id, precio }: { id: string; precio: number | null }) =>
      pedir<void>("PUT", `/api/sistema/precios/${id}`, precio === null ? { aConsultar: true } : { precio }),
    onSuccess: () => consultas.invalidateQueries({ queryKey: PRECIOS }),
  });
}

/** La vista previa es un POST (manda datos), pero no cambia nada: por eso es una mutación sin invalidar. */
export function useVistaPrevia() {
  return useMutation({
    mutationFn: (aumento: Aumento) => pedir<VistaPrevia>("POST", "/api/sistema/precios/aumento/vista-previa", aumento),
  });
}

export function useAplicarAumento() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (aumento: Aumento) => pedir<{ cambiados: number }>("POST", "/api/sistema/precios/aumento", aumento),
    onSuccess: () => consultas.invalidateQueries({ queryKey: PRECIOS }),
  });
}
