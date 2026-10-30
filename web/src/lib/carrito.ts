import { useSyncExternalStore } from "react";
import { formatearPrecio } from "@/lib/formato";

/**
 * El carrito, guardado en el navegador de cada persona (localStorage).
 *
 * Por qué en el navegador y no en el servidor, por ahora: se puede llenar sin tener cuenta,
 * que es como funcionan casi todas las tiendas. Cuando exista el carrito en el backend, al
 * iniciar sesión se va a pasar lo de acá a la cuenta.
 *
 * IMPORTANTE: lo que está acá lo puede modificar cualquiera desde la consola del navegador
 * (incluido el precio). Sirve para mostrar, NUNCA para cobrar: al pagar, el servidor va a
 * volver a calcular todo con los precios reales.
 */

export type ItemCarrito = {
  /** "producto:kit-4-camaras-dvr" o "pc:cpu=...&mother=..." — si ya está, se suma la cantidad. */
  id: string;
  nombre: string;
  precioUnitario: number;
  cantidad: number;
  imagen: string | null;
  /** A dónde lleva al tocar el nombre: el producto, o el armado de la PC. */
  enlace: string;
  /** Líneas de detalle: los componentes de una PC armada, por ejemplo. */
  detalle: string[];
};

export const CANTIDAD_MAXIMA = 20;
const CLAVE = "ram:carrito";
const VACIO: ItemCarrito[] = [];

// React necesita que, si nada cambió, le devolvamos EXACTAMENTE el mismo arreglo (no uno igual):
// por eso se guarda el último texto leído y su resultado, y solo se vuelve a leer si cambió.
let ultimoTexto: string | null = null;
let ultimosItems: ItemCarrito[] = VACIO;
const oyentes = new Set<() => void>();

function leer(): ItemCarrito[] {
  let texto: string | null;
  try {
    texto = localStorage.getItem(CLAVE);
  } catch {
    return ultimosItems; // Almacenamiento bloqueado: el carrito vive en memoria hasta cerrar la pestaña.
  }
  if (texto !== ultimoTexto) {
    ultimoTexto = texto;
    ultimosItems = interpretar(texto);
  }
  return ultimosItems;
}

function guardar(items: ItemCarrito[]) {
  const texto = JSON.stringify(items);
  ultimoTexto = texto;
  ultimosItems = items;
  try {
    localStorage.setItem(CLAVE, texto);
  } catch {
    // Sin almacenamiento: queda en memoria igual.
  }
  oyentes.forEach((avisar) => avisar());
}

/** Lo guardado puede estar roto o haber sido editado a mano: lo que no tiene la forma esperada, se descarta. */
function interpretar(texto: string | null): ItemCarrito[] {
  if (!texto) return VACIO;
  try {
    const datos: unknown = JSON.parse(texto);
    if (!Array.isArray(datos)) return VACIO;
    return datos.filter(
      (i): i is ItemCarrito =>
        typeof i?.id === "string" &&
        typeof i.nombre === "string" &&
        typeof i.precioUnitario === "number" &&
        Number.isInteger(i.cantidad) &&
        i.cantidad > 0 &&
        typeof i.enlace === "string" &&
        Array.isArray(i.detalle),
    );
  } catch {
    return VACIO;
  }
}

function suscribirse(avisar: () => void) {
  oyentes.add(avisar);
  // "storage" se dispara cuando el carrito cambia en OTRA pestaña: así todas muestran lo mismo.
  window.addEventListener("storage", avisar);
  return () => {
    oyentes.delete(avisar);
    window.removeEventListener("storage", avisar);
  };
}

/**
 * El carrito para un componente de React. En el servidor no hay localStorage, así que ahí
 * devuelve null ("todavía no sé"), y la pantalla muestra un estado de carga hasta que el navegador lo lee.
 */
export function useCarrito(): ItemCarrito[] | null {
  return useSyncExternalStore(suscribirse, leer, () => null);
}

export function agregarAlCarrito(item: Omit<ItemCarrito, "cantidad">, cantidad = 1) {
  const items = leer();
  const existente = items.find((i) => i.id === item.id);
  guardar(
    existente
      ? items.map((i) => (i.id === item.id ? { ...i, cantidad: Math.min(i.cantidad + cantidad, CANTIDAD_MAXIMA) } : i))
      : [...items, { ...item, cantidad }],
  );
}

export function cambiarCantidad(id: string, cantidad: number) {
  if (cantidad <= 0) return quitarDelCarrito(id);
  guardar(leer().map((i) => (i.id === id ? { ...i, cantidad: Math.min(cantidad, CANTIDAD_MAXIMA) } : i)));
}

export function quitarDelCarrito(id: string) {
  guardar(leer().filter((i) => i.id !== id));
}

export function vaciarCarrito() {
  guardar(VACIO);
}

export function totalDelCarrito(items: ItemCarrito[]): number {
  return items.reduce((suma, i) => suma + i.precioUnitario * i.cantidad, 0);
}

export function cantidadDeArticulos(items: ItemCarrito[]): number {
  return items.reduce((suma, i) => suma + i.cantidad, 0);
}

/** El pedido escrito para mandar por WhatsApp mientras no hay pago online. */
export function mensajeDelPedido(items: ItemCarrito[]): string {
  const lineas = items.map((i) => {
    const encabezado = `• ${i.cantidad} × ${i.nombre} — ${formatearPrecio(i.precioUnitario * i.cantidad)}`;
    return [encabezado, ...i.detalle.map((d) => `   – ${d}`)].join("\n");
  });
  return ["Hola, quiero hacer este pedido:", "", ...lineas, "", `Total: ${formatearPrecio(totalDelCarrito(items))}`].join("\n");
}
