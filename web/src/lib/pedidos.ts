import { PASOS } from "./arma-tu-pc";
import { enviarFormulario, pedir } from "./api-cliente";
import type { ItemCarrito } from "./carrito";

/**
 * Los pedidos de la tienda (ADR-011): la forma del JSON de /api/tienda/pedidos y cómo se le
 * explica cada estado al cliente.
 */

export type FormaDeEntrega = "RETIRO" | "ENVIO_ROSARIO" | "ENVIO_OTRA_LOCALIDAD";
export type EstadoPedido = "ESPERANDO_PAGO" | "PAGO_INFORMADO" | "PAGADO" | "ENTREGADO" | "CANCELADO";

/** Lo que viaja al servidor por cada cosa del carrito: QUÉ es, nunca cuánto cuesta (el precio lo pone el servidor). */
export type ItemParaElPedido =
  | { tipo: "producto"; slug: string; cantidad: number }
  | { tipo: "pc"; piezas: Record<string, string>; sinPlacaDeVideo: boolean; conArmado: boolean; cantidad: number };

export type NuevoPedido = {
  items: ItemParaElPedido[];
  entrega: FormaDeEntrega;
  direccion?: string;
  telefono: string;
  nota?: string;
};

export type LineaDePedido = {
  descripcion: string;
  detalle: string[];
  referencia: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  /** Faltaba stock al pedirla: se cobra igual y se entrega cuando llega (ADR-014). */
  reserva: boolean;
};

export type Cotizacion = { lineas: LineaDePedido[]; total: number; envioACotizar: boolean };

export type DatosDePago = { alias: string; titular: string; entidad: string; cbu: string | null };

export type Comprobante = {
  id: string;
  nombre: string;
  tipoDeContenido: string;
  tamano: number;
  subidoEn: string;
  rechazado: boolean;
  motivoRechazo: string | null;
  /** Solo en el sistema. */
  url: string | null;
};

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

export type DetalleDePedido = ResumenDePedido & {
  direccion: string | null;
  nota: string | null;
  lineas: LineaDePedido[];
  pagadoEn: string | null;
  entregadoEn: string | null;
  canceladoEn: string | null;
  motivoCancelacion: string | null;
  comprobantes: Comprobante[];
  datosDePago: DatosDePago | null;
  puedeSubirComprobante: boolean;
  puedeCancelar: boolean;
};

// ─── Llamadas ─────────────────────────────────────────────────────────────────────────────

export const cotizar = (pedido: NuevoPedido) => pedir<Cotizacion>("POST", "/api/tienda/pedidos/cotizacion", pedido);
export const realizarPedido = (pedido: NuevoPedido) => pedir<DetalleDePedido>("POST", "/api/tienda/pedidos", pedido);
export const misPedidos = () => pedir<ResumenDePedido[]>("GET", "/api/tienda/pedidos");
export const miPedido = (id: string) => pedir<DetalleDePedido>("GET", `/api/tienda/pedidos/${encodeURIComponent(id)}`);
export const cancelarPedido = (id: string) => pedir<void>("POST", `/api/tienda/pedidos/${encodeURIComponent(id)}/cancelar`);

export function subirComprobante(id: string, archivo: File): Promise<void> {
  const formulario = new FormData();
  formulario.append("archivo", archivo);
  return enviarFormulario<void>(`/api/tienda/pedidos/${encodeURIComponent(id)}/comprobante`, formulario);
}

// ─── Del carrito al pedido ────────────────────────────────────────────────────────────────

/**
 * El carrito guarda en el id QUÉ es cada cosa: "producto:kit-4-camaras-dvr" o "pc:/arma-tu-pc?paso=resumen&cpu=...".
 * De ahí se saca lo que necesita el servidor. Un ítem que no se entiende (un carrito viejo o editado a mano) da null.
 */
export function itemParaElPedido(item: ItemCarrito): ItemParaElPedido | null {
  if (item.id.startsWith("producto:")) {
    const slug = item.id.slice("producto:".length);
    return slug ? { tipo: "producto", slug, cantidad: item.cantidad } : null;
  }
  if (item.id.startsWith("pc:")) {
    const consulta = item.id.slice("pc:".length).split("?")[1] ?? "";
    const parametros = new URLSearchParams(consulta);
    const piezas: Record<string, string> = {};
    for (const { tipo } of PASOS) {
      const valor = parametros.get(tipo);
      // "no" = paso salteado; "integrada" = sin placa de video. Ninguno es una pieza.
      if (valor && valor !== "no" && !(tipo === "gpu" && valor === "integrada")) piezas[tipo] = valor;
    }
    if (Object.keys(piezas).length === 0) return null;
    return {
      tipo: "pc",
      piezas,
      sinPlacaDeVideo: parametros.get("gpu") === "integrada",
      conArmado: parametros.get("armado") !== "no",
      cantidad: item.cantidad,
    };
  }
  return null;
}

// ─── Textos ───────────────────────────────────────────────────────────────────────────────

export const ESTADO_PARA_EL_CLIENTE: Record<EstadoPedido, { titulo: string; detalle: string }> = {
  ESPERANDO_PAGO: {
    titulo: "Esperando tu pago",
    detalle: "Transferí el total al alias y subí el comprobante acá abajo.",
  },
  PAGO_INFORMADO: {
    titulo: "Estamos revisando tu pago",
    detalle: "Recibimos tu comprobante. Te avisamos apenas confirmemos la transferencia.",
  },
  PAGADO: { titulo: "¡Pago confirmado!", detalle: "Estamos preparando tu pedido. Te escribimos para coordinar la entrega." },
  ENTREGADO: { titulo: "Entregado", detalle: "¡Gracias por tu compra!" },
  CANCELADO: { titulo: "Cancelado", detalle: "Este pedido se canceló." },
};

export const ENTREGA: Record<FormaDeEntrega, { titulo: string; detalle: string }> = {
  RETIRO: { titulo: "Retiro en el local", detalle: "Te avisamos cuando esté listo para retirar." },
  ENVIO_ROSARIO: { titulo: "Envío en Rosario", detalle: "Gratis. Coordinamos el día por WhatsApp." },
  ENVIO_OTRA_LOCALIDAD: {
    titulo: "Envío a otra localidad",
    detalle: "Te pasamos el costo por WhatsApp. El envío se paga aparte.",
  },
};
