import type { TipoDeTrabajo } from "@/lib/api";

/** Cómo se nombra cada tipo de trabajo en la tienda. */
export const NOMBRE_DEL_TIPO: Record<TipoDeTrabajo, string> = {
  notebook: "Notebook",
  pc: "PC",
  camaras: "Cámaras",
  alarmas: "Alarmas",
  otro: "Servicio técnico",
};

/** A qué servicio lleva cada tipo, para "¿Necesitás algo parecido?". */
export const SERVICIO_DEL_TIPO: Record<TipoDeTrabajo, { href: string; texto: string }> = {
  notebook: { href: "/servicios#reparacion", texto: "Ver reparación de notebooks y PC" },
  pc: { href: "/servicios#reparacion", texto: "Ver reparación de notebooks y PC" },
  camaras: { href: "/servicios#instalaciones", texto: "Ver instalación de cámaras" },
  alarmas: { href: "/servicios#instalaciones", texto: "Ver instalación de alarmas" },
  otro: { href: "/servicios", texto: "Ver todos los servicios" },
};

const fechaLarga = new Intl.DateTimeFormat("es-AR", { month: "long", year: "numeric", timeZone: "UTC" });

/** "2026-11-17" → "noviembre de 2026". Mes y año alcanzan: el día exacto no le suma nada a quien lee. */
export function mesDelTrabajo(fecha: string): string {
  return fechaLarga.format(new Date(`${fecha}T00:00:00Z`));
}
