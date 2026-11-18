import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { enviarFormulario, pedir } from "./cliente";

/** Trabajos realizados que se publican en la tienda (/trabajos). ADR-018. */

export type TipoDeTrabajo = "NOTEBOOK" | "PC" | "CAMARAS" | "ALARMAS" | "OTRO";

export type DatosDeTrabajo = {
  tipo: TipoDeTrabajo;
  titulo: string;
  /** Barrio o localidad, escrito libre. Nunca calle ni altura. */
  barrio: string;
  aDomicilio: boolean;
  problema: string;
  diagnostico: string | null;
  solucion: string;
  /** El link de un reel de Instagram o un video de YouTube, o null. El backend lo guarda limpio. */
  video: string | null;
  /** "2026-11-17" */
  fecha: string;
  /** Solo para el taller: no se publica. */
  numeroDeOrden: number | null;
  /** El cliente aceptó que se publique. */
  consentimiento: boolean;
  publicado: boolean;
};

export type Trabajo = DatosDeTrabajo & {
  id: string;
  slug: string;
  /** La primera foto en chico, o null. */
  miniatura: string | null;
  fotos: { id: string; url: string }[];
};

const TRABAJOS = ["trabajos"] as const;

export function useTrabajos() {
  return useQuery({ queryKey: [...TRABAJOS, "lista"], queryFn: () => pedir<Trabajo[]>("GET", "/api/sistema/trabajos") });
}

export function useTrabajo(id: string | undefined) {
  return useQuery({
    queryKey: [...TRABAJOS, "detalle", id],
    queryFn: () => pedir<Trabajo>("GET", `/api/sistema/trabajos/${id}`),
    enabled: !!id,
  });
}

/** Los barrios ya usados, para sugerirlos mientras se escribe. */
export function useBarrios() {
  return useQuery({ queryKey: [...TRABAJOS, "barrios"], queryFn: () => pedir<string[]>("GET", "/api/sistema/trabajos/barrios") });
}

function useInvalidar() {
  const consultas = useQueryClient();
  return () => consultas.invalidateQueries({ queryKey: TRABAJOS });
}

export function useCrearTrabajo() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (datos: DatosDeTrabajo) => pedir<{ id: string }>("POST", "/api/sistema/trabajos", datos),
    onSuccess: invalidar,
  });
}

export function useEditarTrabajo(id: string) {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (datos: DatosDeTrabajo) => pedir<void>("PUT", `/api/sistema/trabajos/${id}`, datos),
    onSuccess: invalidar,
  });
}

export function useBorrarTrabajo(id: string) {
  const invalidar = useInvalidar();
  return useMutation({ mutationFn: () => pedir<void>("DELETE", `/api/sistema/trabajos/${id}`), onSuccess: invalidar });
}

export function useFotosDelTrabajo(id: string) {
  const invalidar = useInvalidar();
  const subir = useMutation({
    mutationFn: (archivo: File) => {
      const formulario = new FormData();
      formulario.append("foto", archivo);
      return enviarFormulario<{ id: string }>("POST", `/api/sistema/trabajos/${id}/fotos`, formulario);
    },
    onSuccess: invalidar,
  });
  const borrar = useMutation({
    mutationFn: (foto: string) => pedir<void>("DELETE", `/api/sistema/trabajos/${id}/fotos/${foto}`),
    onSuccess: invalidar,
  });
  const ordenar = useMutation({
    mutationFn: (orden: string[]) => pedir<void>("PUT", `/api/sistema/trabajos/${id}/fotos`, { orden }),
    onSuccess: invalidar,
  });
  return { subir, borrar, ordenar };
}

export const NOMBRE_DEL_TIPO_DE_TRABAJO: Record<TipoDeTrabajo, string> = {
  NOTEBOOK: "Notebook",
  PC: "PC",
  CAMARAS: "Cámaras",
  ALARMAS: "Alarmas",
  OTRO: "Otro",
};
