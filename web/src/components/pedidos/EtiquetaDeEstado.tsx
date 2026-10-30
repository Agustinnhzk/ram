import type { EstadoPedido } from "@/lib/pedidos";

const ESTILOS: Record<EstadoPedido, { texto: string; clase: string }> = {
  ESPERANDO_PAGO: { texto: "Esperando pago", clase: "border-oro/60 text-oro" },
  PAGO_INFORMADO: { texto: "Revisando pago", clase: "border-principal/40 text-principal" },
  PAGADO: { texto: "Pagado", clase: "border-oro bg-oro/15 text-principal" },
  ENTREGADO: { texto: "Entregado", clase: "border-borde text-suave" },
  CANCELADO: { texto: "Cancelado", clase: "border-red-500/60 text-red-500" },
};

export function EtiquetaDeEstado({ estado }: { estado: EstadoPedido }) {
  const { texto, clase } = ESTILOS[estado];
  return <span className={`inline-block rounded-full border px-2.5 py-0.5 text-xs font-semibold whitespace-nowrap ${clase}`}>{texto}</span>;
}
