import type { DetalleDeOrden } from "../api/reparaciones";
import { formatearPrecio } from "./formato";
import { linkDeSeguimiento } from "./reparaciones";

/**
 * Los mensajes de WhatsApp que se le mandan al cliente. Están todos juntos para que sea fácil
 * ajustarles el tono. Nunca llevan el password ni las observaciones internas.
 */

function primerNombre(orden: DetalleDeOrden): string {
  return orden.cliente?.nombreCompleto.split(" ")[0] ?? "";
}

export function mensajeDeIngreso(orden: DetalleDeOrden, taller: string): string {
  return [
    `¡Hola ${primerNombre(orden)}! Recibimos tu ${orden.equipo} en ${taller}.`,
    `Tu número de orden es *${orden.numero}*.`,
    `Podés ver cómo va la reparación en ${linkDeSeguimiento(orden.numero)} con los últimos 4 números de tu teléfono.`,
  ].join("\n");
}

export function mensajeDePresupuesto(orden: DetalleDeOrden): string {
  return [
    `¡Hola ${primerNombre(orden)}! Ya revisamos tu ${orden.equipo} (orden N° ${orden.numero}).`,
    `Presupuesto: ${orden.presupuestoDescripcion} — *${formatearPrecio(orden.presupuestoMonto)}*.`,
    "¿Lo aprobás así lo reparamos?",
  ].join("\n");
}

export function mensajeDeRetiro(orden: DetalleDeOrden): string {
  const total =
    orden.estado === "LISTO_PARA_RETIRAR" && orden.presupuestoMonto !== null && orden.movimientos.some((m) => m.estado === "APROBADO")
      ? ` El total es ${formatearPrecio(orden.presupuestoMonto)}.`
      : "";
  return `¡Hola ${primerNombre(orden)}! Tu ${orden.equipo} (orden N° ${orden.numero}) ya está listo para retirar.${total} ¡Te esperamos!`;
}

export function mensajeDeSeguimiento(orden: DetalleDeOrden): string {
  return `¡Hola ${primerNombre(orden)}! Podés ver cómo va tu reparación (orden N° ${orden.numero}) en ${linkDeSeguimiento(orden.numero)} con los últimos 4 números de tu teléfono.`;
}
