import type { Estado } from "../api/reparaciones";

/** Cómo se llama cada estado en pantalla, y de qué color es su etiqueta. */
export const ESTADOS: Record<Estado, { nombre: string; tono: Tono }> = {
  INGRESADO: { nombre: "Ingresado", tono: "neutro" },
  EN_DIAGNOSTICO: { nombre: "En diagnóstico", tono: "trabajo" },
  PRESUPUESTADO: { nombre: "Presupuestado", tono: "espera" },
  APROBADO: { nombre: "Aprobado", tono: "trabajo" },
  RECHAZADO: { nombre: "Rechazado", tono: "neutro" },
  EN_REPARACION: { nombre: "En reparación", tono: "trabajo" },
  ESPERANDO_REPUESTO: { nombre: "Esperando repuesto", tono: "espera" },
  EN_PROVEEDOR: { nombre: "En proveedor", tono: "espera" },
  SIN_REPARACION: { nombre: "Sin reparación", tono: "neutro" },
  LISTO_PARA_RETIRAR: { nombre: "Listo para retirar", tono: "listo" },
  ENTREGADO: { nombre: "Entregado", tono: "final" },
};

export type Tono = "neutro" | "trabajo" | "espera" | "listo" | "final";

/** El texto del botón para pasar a cada estado ("Empezar diagnóstico", no "En diagnóstico"). */
export const ACCION_HACIA: Record<Estado, string> = {
  INGRESADO: "Ingresar",
  EN_DIAGNOSTICO: "Empezar diagnóstico",
  PRESUPUESTADO: "Cargar presupuesto",
  APROBADO: "El cliente aprobó",
  RECHAZADO: "El cliente rechazó",
  EN_REPARACION: "Reparar",
  ESPERANDO_REPUESTO: "Esperando repuesto",
  EN_PROVEEDOR: "Enviar a proveedor",
  SIN_REPARACION: "No tiene arreglo",
  LISTO_PARA_RETIRAR: "Listo para retirar",
  ENTREGADO: "Entregar",
};

/**
 * Un link de WhatsApp a un teléfono de Argentina. Los teléfonos se guardan con solo dígitos ("3415551234");
 * para WhatsApp hace falta el código de país y el 9 de los celulares: 54 9 341 555 1234.
 * Si ya viene con "+", se usa tal cual (un número de otro país).
 */
export function linkWhatsApp(telefono: string, mensaje: string): string {
  const numero = telefono.startsWith("+") ? telefono.slice(1) : `549${telefono.replace(/^0/, "")}`;
  return `https://wa.me/${numero}?text=${encodeURIComponent(mensaje)}`;
}

/** Dónde está la tienda (la página de seguimiento). En producción se define VITE_URL_TIENDA al compilar. */
export const URL_TIENDA = import.meta.env.VITE_URL_TIENDA ?? "http://localhost:3000";

export function linkDeSeguimiento(numero: number): string {
  return `${URL_TIENDA}/seguimiento?orden=${numero}`;
}

/** "2026-12-28" → "28/12/2026" (sin pasar por Date, para que no cambie de día por la zona horaria). */
export function formatearDia(iso: string): string {
  const [anio, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${anio}`;
}
