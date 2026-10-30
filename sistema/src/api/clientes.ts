import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";
import type { ResumenDePedido } from "./pedidos";
import type { ResumenDeOrden } from "./reparaciones";

/** Las formas del JSON de /api/sistema/clientes (ver ClientesController en el backend, ADR-014). */

export type CuentaVinculada = {
  id: string;
  /** En el listado vienen null: solo se sabe QUE tiene cuenta. En la ficha vienen completos. */
  nombre: string | null;
  correo: string | null;
};

export type FichaDeCliente = {
  id: string;
  nombreCompleto: string;
  telefono: string;
  correo: string | null;
  notas: string | null;
  cuenta: CuentaVinculada | null;
  creadoEn: string | null;
};

export type DatosDeFicha = { nombreCompleto: string; telefono: string; correo: string | null; notas: string | null };

const CLIENTES = ["clientes"] as const;

/** Sin texto: los últimos cargados. Con números: por teléfono. Si no, por nombre. */
export function useClientes(texto: string) {
  return useQuery({
    queryKey: [...CLIENTES, "lista", texto],
    queryFn: () => pedir<FichaDeCliente[]>("GET", `/api/sistema/clientes?texto=${encodeURIComponent(texto)}`),
    placeholderData: (anterior) => anterior,
  });
}

export function useFichaDeCliente(id: string) {
  return useQuery({
    queryKey: [...CLIENTES, "ficha", id],
    queryFn: () => pedir<FichaDeCliente>("GET", `/api/sistema/clientes/${id}`),
  });
}

export function useReparacionesDeCliente(id: string) {
  return useQuery({
    queryKey: ["reparaciones", "de-cliente", id],
    queryFn: () => pedir<ResumenDeOrden[]>("GET", `/api/sistema/reparaciones?cliente=${id}`),
  });
}

/** Los pedidos de la tienda de la cuenta vinculada (si no tiene cuenta, no se pide nada). */
export function usePedidosDeCuenta(cuenta: string | undefined) {
  return useQuery({
    queryKey: ["pedidos", "de-cuenta", cuenta],
    queryFn: () => pedir<ResumenDePedido[]>("GET", `/api/sistema/pedidos?cuenta=${cuenta}`),
    enabled: !!cuenta,
  });
}

export function useCrearCliente() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (datos: { nombreCompleto: string; telefono: string; correo: string | null }) =>
      pedir<{ id: string }>("POST", "/api/sistema/clientes", datos),
    onSuccess: () => consultas.invalidateQueries({ queryKey: CLIENTES }),
  });
}

export function useEditarCliente(id: string) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (datos: DatosDeFicha) => pedir<void>("PUT", `/api/sistema/clientes/${id}`, datos),
    // La reparación muestra el nombre y el teléfono del cliente: también se refresca.
    onSuccess: () => Promise.all([consultas.invalidateQueries({ queryKey: CLIENTES }), consultas.invalidateQueries({ queryKey: ["reparaciones"] })]),
  });
}

/** Borra la ficha y su cuenta de la tienda (solo Dueño). Si tiene pedidos o reparaciones, el backend no deja. */
export function useBorrarCliente(id: string) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: () => pedir<void>("DELETE", `/api/sistema/clientes/${id}`),
    // Solo los listados: la ficha borrada no se vuelve a pedir (daría 404 mientras la pantalla se cierra).
    onSuccess: () => consultas.invalidateQueries({ queryKey: [...CLIENTES, "lista"] }),
  });
}

export function useVincularCuenta(id: string) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: async (correo: string | null) => {
      if (correo === null) await pedir<void>("DELETE", `/api/sistema/clientes/${id}/cuenta`);
      else await pedir<void>("PUT", `/api/sistema/clientes/${id}/cuenta`, { correo });
    },
    onSuccess: () => consultas.invalidateQueries({ queryKey: CLIENTES }),
  });
}
