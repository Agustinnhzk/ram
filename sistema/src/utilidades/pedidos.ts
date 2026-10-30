import type { EstadoPedido, FormaDeEntrega } from "../api/pedidos";
import type { Tono } from "./reparaciones";

export const ESTADOS_DE_PEDIDO: Record<EstadoPedido, { nombre: string; tono: Tono }> = {
  ESPERANDO_PAGO: { nombre: "Esperando pago", tono: "espera" },
  PAGO_INFORMADO: { nombre: "Revisar pago", tono: "trabajo" },
  PAGADO: { nombre: "Pagado · a entregar", tono: "listo" },
  ENTREGADO: { nombre: "Entregado", tono: "final" },
  CANCELADO: { nombre: "Cancelado", tono: "neutro" },
};

export const ENTREGAS: Record<FormaDeEntrega, string> = {
  RETIRO: "Retira en el local",
  ENVIO_ROSARIO: "Envío en Rosario (gratis)",
  ENVIO_OTRA_LOCALIDAD: "Envío fuera de Rosario (a cotizar)",
};

/**
 * El teléfono del pedido lo escribió el cliente como quiso ("341 555-1234", "+54 9 341..."): se dejan los dígitos
 * y, si no trae el 54 de Argentina, se agrega 549 (celular).
 */
export function linkWhatsAppDelCliente(telefono: string, mensaje: string): string {
  const digitos = telefono.replace(/\D/g, "").replace(/^0/, "");
  const numero = digitos.startsWith("54") ? digitos : `549${digitos}`;
  return `https://wa.me/${numero}?text=${encodeURIComponent(mensaje)}`;
}
