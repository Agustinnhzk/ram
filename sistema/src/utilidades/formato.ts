const pesos = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });
const fechaYHora = new Intl.DateTimeFormat("es-AR", { dateStyle: "short", timeStyle: "short" });

/** 45000 → "$ 45.000"; null → "A consultar" */
export function formatearPrecio(precio: number | null): string {
  return precio === null ? "A consultar" : pesos.format(precio);
}

/** "2026-10-29T17:30:00Z" → "29/10/26, 14:30" (en la hora de quien mira) */
export function formatearFecha(iso: string): string {
  return fechaYHora.format(new Date(iso));
}

/**
 * Lo que se escribe en un campo de precio → número de pesos, o null si no es un número válido.
 * Acepta "45000", "45.000" y "$ 45.000". No acepta centavos: "45000,50" es inválido (ADR-008).
 */
export function leerPrecio(texto: string): number | null {
  const limpio = texto.replace(/[$\s.]/g, "");
  if (!/^\d{1,13}$/.test(limpio)) return null;
  return Number(limpio);
}

/** "8" → 8; "8,5" → 8.5; "-10" → -10. null si no es un porcentaje válido. */
export function leerPorcentaje(texto: string): number | null {
  const limpio = texto.trim().replace(",", ".").replace("%", "");
  if (!/^-?\d{1,3}(\.\d{1,2})?$/.test(limpio)) return null;
  const valor = Number(limpio);
  return valor === 0 ? null : valor;
}
