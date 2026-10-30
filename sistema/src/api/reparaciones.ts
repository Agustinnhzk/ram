import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";

/** Las formas del JSON de /api/sistema/reparaciones (ver ReparacionesController en el backend). */

export type Estado =
  | "INGRESADO"
  | "EN_DIAGNOSTICO"
  | "PRESUPUESTADO"
  | "APROBADO"
  | "RECHAZADO"
  | "EN_REPARACION"
  | "ESPERANDO_REPUESTO"
  | "EN_PROVEEDOR"
  | "SIN_REPARACION"
  | "LISTO_PARA_RETIRAR"
  | "ENTREGADO";

export type Vista = "EN_TALLER" | "ENTREGADAS" | "TODAS";

export type ResumenDeOrden = {
  id: string;
  numero: number;
  cliente: string;
  telefono: string | null;
  equipo: string;
  estado: Estado;
  ingresadoEn: string;
  garantiaHasta: string | null;
  enGarantia: boolean;
};

export type ClienteDeOrden = { id: string; nombreCompleto: string; telefono: string; correo: string | null };

export type Movimiento = {
  /** null = una nota suelta, sin cambio de estado */
  estado: Estado | null;
  nota: string | null;
  paraCliente: boolean;
  usuario: string;
  cuando: string;
};

export type DetalleDeOrden = {
  id: string;
  numero: number;
  cliente: ClienteDeOrden | null;
  equipo: string;
  conCargador: boolean;
  procesador: string | null;
  /** null si no tiene o si tu rol no lo puede ver (passwordVisible) */
  password: string | null;
  passwordVisible: boolean;
  problema: string;
  observaciones: string | null;
  estado: Estado;
  siguientes: Estado[];
  presupuestoMonto: number | null;
  presupuestoDescripcion: string | null;
  ingresadoEn: string;
  entregadoEn: string | null;
  garantiaDias: number | null;
  /** "2026-12-28" */
  garantiaHasta: string | null;
  enGarantia: boolean;
  movimientos: Movimiento[];
};

export type NuevoIngreso = {
  numero: number;
  clienteId?: string;
  clienteNuevo?: { nombreCompleto: string; telefono: string; correo: string | null };
  equipo: string;
  conCargador: boolean;
  procesador: string | null;
  password: string | null;
  problema: string;
  observaciones: string | null;
};

const REPARACIONES = ["reparaciones"] as const;

export function useOrdenes(vista: Vista, texto: string) {
  const parametros = new URLSearchParams({ vista });
  if (texto.trim()) parametros.set("q", texto.trim());
  return useQuery({
    queryKey: [...REPARACIONES, "lista", vista, texto.trim()],
    queryFn: () => pedir<ResumenDeOrden[]>("GET", `/api/sistema/reparaciones?${parametros}`),
    // Mientras escribís en el buscador, la tabla muestra lo anterior en vez de quedar vacía.
    placeholderData: (anterior) => anterior,
  });
}

export function useOrden(id: string) {
  return useQuery({
    queryKey: [...REPARACIONES, "detalle", id],
    queryFn: () => pedir<DetalleDeOrden>("GET", `/api/sistema/reparaciones/${id}`),
  });
}

export function useUltimoNumero() {
  return useQuery({
    queryKey: [...REPARACIONES, "ultimo-numero"],
    queryFn: () => pedir<{ ultimo: number | null }>("GET", "/api/sistema/reparaciones/ultimo-numero"),
  });
}

export function useRegistrarIngreso() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (ingreso: NuevoIngreso) => pedir<{ id: string }>("POST", "/api/sistema/reparaciones", ingreso),
    onSuccess: () => consultas.invalidateQueries({ queryKey: REPARACIONES }),
  });
}

/**
 * Todas las acciones sobre una orden (cambiar estado, presupuestar, entregar, anotar) son POST a una
 * sub-ruta. Al terminar se invalida TODO lo de reparaciones: el detalle y los listados se vuelven a pedir.
 */
export function useAccionSobreOrden<T>(id: string, accion: "estado" | "presupuesto" | "entrega" | "notas") {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (cuerpo: T) => pedir<void>("POST", `/api/sistema/reparaciones/${id}/${accion}`, cuerpo),
    onSuccess: () => consultas.invalidateQueries({ queryKey: REPARACIONES }),
  });
}

/** El buscador de "cliente que vuelve": GET /api/sistema/clientes?nombre=... (desde 2 letras). */
export function useBuscarClientes(texto: string) {
  const buscado = texto.trim();
  return useQuery({
    queryKey: ["clientes", "buscar", buscado],
    queryFn: () => pedir<ClienteDeOrden[]>("GET", `/api/sistema/clientes?nombre=${encodeURIComponent(buscado)}`),
    enabled: buscado.length >= 2,
    staleTime: 30_000,
  });
}

/** Nombre y slug del taller (para el comprobante). */
export function useTaller() {
  return useQuery({
    queryKey: ["taller"],
    queryFn: () => pedir<{ nombre: string; slug: string }>("GET", "/api/publico/taller"),
    staleTime: Infinity,
  });
}
