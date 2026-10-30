import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";
import type { TipoComponente } from "./productos";

/** Las formas del JSON de /api/sistema/stock (ver StockController en el backend, ADR-014). */

export type EstadoDeStock = "DISPONIBLE" | "ULTIMAS_UNIDADES" | "SIN_STOCK";

export type FilaDeStock = {
  id: string;
  slug: string;
  nombre: string;
  categoria: string;
  tipoComponente: TipoComponente | null;
  publicado: boolean;
  /** null = sin control de stock (se vende sin límite). */
  cantidad: number | null;
  /** Unidades pagadas que esperan mercadería. */
  reservado: number;
  estado: EstadoDeStock | null;
};

export type TipoDeMovimiento = "INICIAL" | "INGRESO" | "AJUSTE" | "VENTA" | "RESERVA_CUBIERTA" | "DEVOLUCION";

export type MovimientoDeStock = {
  fecha: string;
  tipo: TipoDeMovimiento;
  cantidad: number;
  saldo: number;
  nota: string | null;
  numeroPedido: number | null;
  usuario: string | null;
};

export type EstadoDeReserva = "PENDIENTE" | "CUBIERTA" | "CANCELADA";

export type Reserva = {
  id: string;
  producto: string;
  productoNombre: string;
  pedido: string;
  numeroPedido: number;
  cantidad: number;
  estado: EstadoDeReserva;
  creadaEn: string;
  resueltaEn: string | null;
  sePuedeCubrir: boolean;
};

const STOCK = ["stock"] as const;

export function useStock() {
  return useQuery({ queryKey: [...STOCK, "lista"], queryFn: () => pedir<FilaDeStock[]>("GET", "/api/sistema/stock") });
}

export function useMovimientos(producto: string | null) {
  return useQuery({
    queryKey: [...STOCK, "movimientos", producto],
    queryFn: () => pedir<MovimientoDeStock[]>("GET", `/api/sistema/stock/${producto}/movimientos`),
    enabled: !!producto,
  });
}

export function useReservasPendientes() {
  return useQuery({ queryKey: [...STOCK, "reservas"], queryFn: () => pedir<Reserva[]>("GET", "/api/sistema/stock/reservas") });
}

export function useReservasDePedido(pedido: string) {
  return useQuery({
    queryKey: [...STOCK, "reservas", pedido],
    queryFn: () => pedir<Reserva[]>("GET", `/api/sistema/stock/reservas?pedido=${pedido}`),
  });
}

export type CambioDeStock =
  | { tipo: "controlar"; producto: string; cantidad: number }
  | { tipo: "ingresar"; producto: string; cantidad: number; nota: string }
  | { tipo: "ajustar"; producto: string; cantidad: number; motivo: string }
  | { tipo: "dejar"; producto: string }
  | { tipo: "cubrir"; reserva: string };

/** Todo lo que cambia el stock pasa por acá. Después se refrescan el stock y los pedidos (por las reservas). */
export function useCambiarStock() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: async (cambio: CambioDeStock) => {
      switch (cambio.tipo) {
        case "controlar":
          return pedir<void>("POST", `/api/sistema/stock/${cambio.producto}`, { cantidad: cambio.cantidad });
        case "ingresar":
          return pedir<void>("POST", `/api/sistema/stock/${cambio.producto}/ingresos`, { cantidad: cambio.cantidad, nota: cambio.nota });
        case "ajustar":
          return pedir<void>("PUT", `/api/sistema/stock/${cambio.producto}`, { cantidad: cambio.cantidad, motivo: cambio.motivo });
        case "dejar":
          return pedir<void>("DELETE", `/api/sistema/stock/${cambio.producto}`);
        case "cubrir":
          return pedir<void>("POST", `/api/sistema/stock/reservas/${cambio.reserva}/cubrir`);
      }
    },
    onSuccess: () => consultas.invalidateQueries({ queryKey: STOCK }),
  });
}
