import type { EstadoDeStock, TipoDeMovimiento } from "../api/stock";

export const NOMBRE_DEL_MOVIMIENTO: Record<TipoDeMovimiento, string> = {
  INICIAL: "Inicio del control",
  INGRESO: "Llegó mercadería",
  AJUSTE: "Ajuste por conteo",
  VENTA: "Venta",
  RESERVA_CUBIERTA: "Reserva cubierta",
  DEVOLUCION: "Pedido cancelado",
};

/** Cómo se ve cada estado (lo mismo que ve la tienda). */
export const ESTILO_DEL_ESTADO: Record<EstadoDeStock, { texto: string; clases: string }> = {
  DISPONIBLE: { texto: "Hay", clases: "border-exito/60 text-exito" },
  ULTIMAS_UNIDADES: { texto: "Pocas", clases: "border-aviso/60 text-aviso" },
  SIN_STOCK: { texto: "Sin stock", clases: "border-peligro/60 text-peligro" },
};

/** "5" → 5. null si no es un entero entre 0 y 100.000 (lo mismo que acepta el backend). */
export function leerCantidad(texto: string): number | null {
  const limpio = texto.trim();
  if (!/^\d{1,6}$/.test(limpio)) return null;
  const valor = Number(limpio);
  return valor <= 100_000 ? valor : null;
}
