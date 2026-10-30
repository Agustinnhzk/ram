import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";

/** Las formas del JSON de /api/sistema/instalaciones y /api/sistema/monitoreo (ADR-015). */

export type EstadoInstalacion = "VISITA" | "PRESUPUESTO" | "INSTALACION" | "TERMINADA";

export type Instalacion = {
  id: string;
  numero: number;
  clienteId: string;
  cliente: string;
  telefono: string | null;
  direccion: string;
  estado: EstadoInstalacion;
  equipos: string | null;
  camaras: number | null;
  /** "2026-11-03" */
  fecha: string | null;
  tecnico: string | null;
  presupuesto: number | null;
  notas: string | null;
  creadaEn: string;
};

export type DatosDeInstalacion = {
  clienteId?: string;
  clienteNuevo?: { nombreCompleto: string; telefono: string };
  direccion: string;
  estado: EstadoInstalacion;
  equipos: string | null;
  camaras: number | null;
  fecha: string | null;
  tecnico: string | null;
  presupuesto: number | null;
  notas: string | null;
};

export type FilaDeAbonos = {
  id: string;
  clienteId: string;
  cliente: string;
  telefono: string | null;
  direccion: string | null;
  monto: number | null;
  activo: boolean;
  /** 12 valores: enero … diciembre. */
  pagos: boolean[];
};

export const ESTADOS_DE_INSTALACION: Record<EstadoInstalacion, string> = {
  VISITA: "Visita",
  PRESUPUESTO: "Presupuesto",
  INSTALACION: "Instalación",
  TERMINADA: "Terminada",
};

export function useInstalaciones(terminadas: boolean) {
  return useQuery({
    queryKey: ["instalaciones", "lista", terminadas],
    queryFn: () => pedir<Instalacion[]>("GET", `/api/sistema/instalaciones?terminadas=${terminadas}`),
  });
}

export function useInstalacion(id: string | undefined) {
  return useQuery({
    queryKey: ["instalaciones", "detalle", id],
    queryFn: () => pedir<Instalacion>("GET", `/api/sistema/instalaciones/${id}`),
    enabled: !!id,
  });
}

export function useGuardarInstalacion(id: string | undefined) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: async (datos: DatosDeInstalacion): Promise<string> => {
      if (id) {
        await pedir<void>("PUT", `/api/sistema/instalaciones/${id}`, datos);
        return id;
      }
      return (await pedir<Instalacion>("POST", "/api/sistema/instalaciones", datos)).id;
    },
    onSuccess: () => Promise.all([consultas.invalidateQueries({ queryKey: ["instalaciones"] }), consultas.invalidateQueries({ queryKey: ["clientes"] })]),
  });
}

export function useAbonos(anio: number) {
  return useQuery({ queryKey: ["monitoreo", anio], queryFn: () => pedir<FilaDeAbonos[]>("GET", `/api/sistema/monitoreo?anio=${anio}`) });
}

export function useAgregarAbonado() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (datos: { clienteId?: string; clienteNuevo?: { nombreCompleto: string; telefono: string }; direccion: string | null; monto: number | null }) =>
      pedir<{ id: string }>("POST", "/api/sistema/monitoreo", { ...datos, activo: true }),
    onSuccess: () => consultas.invalidateQueries({ queryKey: ["monitoreo"] }),
  });
}

export function useEditarAbonado() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...datos }: { id: string; direccion: string | null; monto: number | null; activo: boolean }) =>
      pedir<void>("PUT", `/api/sistema/monitoreo/${id}`, datos),
    onSuccess: () => consultas.invalidateQueries({ queryKey: ["monitoreo"] }),
  });
}

export function useMarcarPago(anio: number) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: ({ abonado, mes, pagado }: { abonado: string; mes: number; pagado: boolean }) =>
      pedir<void>("PUT", `/api/sistema/monitoreo/${abonado}/pagos/${anio}/${mes}`, { pagado }),
    onSuccess: () => consultas.invalidateQueries({ queryKey: ["monitoreo", anio] }),
  });
}
