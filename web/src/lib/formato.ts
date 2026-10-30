const precioEnPesos = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });

/** 1250000 → "$ 1.250.000" */
export function formatearPrecio(precio: number): string {
  return precioEnPesos.format(precio);
}
