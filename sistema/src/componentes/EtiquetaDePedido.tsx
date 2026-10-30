import type { EstadoPedido } from "../api/pedidos";
import { ESTADOS_DE_PEDIDO } from "../utilidades/pedidos";
import type { Tono } from "../utilidades/reparaciones";

// Los mismos colores que las reparaciones: "espera" = falta algo del cliente, "trabajo" = te toca a vos, "listo" = a entregar.
const COLORES: Record<Tono, string> = {
  neutro: "border-borde text-suave",
  trabajo: "border-info/50 text-info",
  espera: "border-aviso/60 text-aviso",
  listo: "border-exito/60 text-exito",
  final: "border-borde bg-superficie-alta text-principal",
};

export function EtiquetaDePedido({ estado }: { estado: EstadoPedido }) {
  const { nombre, tono } = ESTADOS_DE_PEDIDO[estado];
  return (
    <span className={`inline-block rounded-full border px-2.5 py-0.5 text-xs font-medium whitespace-nowrap ${COLORES[tono]}`}>
      {nombre}
    </span>
  );
}
