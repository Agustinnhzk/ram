import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { pedir } from "./cliente";

/** Las formas del JSON de /api/sistema/pedidos (ver SistemaPedidosController en el backend, ADR-011). */

export type EstadoPedido = "ESPERANDO_PAGO" | "PAGO_INFORMADO" | "PAGADO" | "ENTREGADO" | "CANCELADO";
export type FormaDeEntrega = "RETIRO" | "ENVIO_ROSARIO" | "ENVIO_OTRA_LOCALIDAD";

export type ResumenDePedido = {
  id: string;
  numero: number;
  cliente: string;
  telefono: string;
  entrega: FormaDeEntrega;
  total: number;
  estado: EstadoPedido;
  creadoEn: string;
};

export type LineaDePedido = {
  descripcion: string;
  detalle: string[];
  referencia: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  /** Al pedirla faltaba stock: se entrega cuando llega (ADR-014). */
  reserva: boolean;
};

export type Comprobante = {
  id: string;
  nombre: string;
  tipoDeContenido: string;
  tamano: number;
  subidoEn: string;
  rechazado: boolean;
  motivoRechazo: string | null;
  /** /api/sistema/pedidos/{id}/comprobantes/{cid}: se abre con la sesión del sistema. */
  url: string;
};

export type DetalleDePedido = ResumenDePedido & {
  direccion: string | null;
  nota: string | null;
  lineas: LineaDePedido[];
  pagadoEn: string | null;
  entregadoEn: string | null;
  canceladoEn: string | null;
  motivoCancelacion: string | null;
  comprobantes: Comprobante[];
};

const PEDIDOS = ["pedidos"] as const;

/** estado null = los activos (falta pagar o entregar). */
export function usePedidos(estado: EstadoPedido | null) {
  return useQuery({
    queryKey: [...PEDIDOS, "lista", estado],
    queryFn: () => pedir<ResumenDePedido[]>("GET", `/api/sistema/pedidos${estado ? `?estado=${estado}` : ""}`),
    // Un comprobante nuevo puede llegar en cualquier momento: se vuelve a pedir cada minuto mientras la pantalla está abierta.
    refetchInterval: 60_000,
  });
}

export function usePedido(id: string) {
  return useQuery({
    queryKey: [...PEDIDOS, "detalle", id],
    queryFn: () => pedir<DetalleDePedido>("GET", `/api/sistema/pedidos/${id}`),
  });
}

/** Lo borra del todo, con sus comprobantes (solo Dueño, para limpiar pruebas). Si estaba pagado, devuelve el stock. */
export function useBorrarPedido(id: string) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: () => pedir<void>("DELETE", `/api/sistema/pedidos/${id}`),
    // Solo los listados y el stock: el detalle borrado no se vuelve a pedir (daría 404 mientras la pantalla se cierra).
    onSuccess: () =>
      Promise.all([
        consultas.invalidateQueries({ queryKey: [...PEDIDOS, "lista"] }),
        consultas.invalidateQueries({ queryKey: [...PEDIDOS, "de-cuenta"] }),
        consultas.invalidateQueries({ queryKey: ["stock"] }),
      ]),
  });
}

export type AccionDePedido = "confirmar-pago" | "rechazar-comprobante" | "entregar" | "cancelar";

/** Todas las acciones son POST a una sub-ruta; rechazar y cancelar llevan {"motivo": "..."}. */
export function useAccionSobrePedido(id: string) {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: ({ accion, motivo }: { accion: AccionDePedido; motivo?: string }) =>
      pedir<void>("POST", `/api/sistema/pedidos/${id}/${accion}`, motivo === undefined ? undefined : { motivo }),
    // Confirmar o cancelar mueve el stock y las reservas (ADR-014): también se refrescan.
    onSuccess: () => Promise.all([consultas.invalidateQueries({ queryKey: PEDIDOS }), consultas.invalidateQueries({ queryKey: ["stock"] })]),
  });
}
