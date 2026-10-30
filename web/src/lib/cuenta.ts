import { useSyncExternalStore } from "react";
import { ErrorDeApi, pedir } from "./api-cliente";

/**
 * La sesión del cliente en la tienda (ADR-006). La cookie la maneja el navegador; acá solo se guarda
 * QUIÉN está logueado, para mostrarlo en el encabezado y para saber si el checkout puede seguir.
 *
 * Es un "almacén" chico, como el del carrito: cualquier componente lo lee con useSesion() y todos se
 * enteran cuando alguien entra o sale.
 */

export type Cliente = { id: string; nombre: string };

/** undefined: todavía no se preguntó al servidor. null: no hay nadie logueado. */
type Estado = Cliente | null | undefined;

let estado: Estado = undefined;
let consulta: Promise<void> | null = null;
const oyentes = new Set<() => void>();

function cambiar(nuevo: Estado) {
  estado = nuevo;
  oyentes.forEach((avisar) => avisar());
}

function suscribirse(avisar: () => void) {
  oyentes.add(avisar);
  if (estado === undefined && !consulta) {
    // Sin sesión, el backend responde 204 (vacío): llega undefined.
    consulta = pedir<Cliente | undefined>("GET", "/api/tienda/sesion")
      .then((cliente) => cambiar(cliente ? { id: cliente.id, nombre: cliente.nombre } : null))
      .catch(() => cambiar(null))
      .finally(() => (consulta = null));
  }
  return () => oyentes.delete(avisar);
}

/** undefined mientras se consulta (un instante); null si no hay sesión. En el servidor, siempre undefined. */
export function useSesion(): Estado {
  return useSyncExternalStore(
    suscribirse,
    () => estado,
    () => undefined,
  );
}

export async function ingresar(correo: string, clave: string): Promise<void> {
  const cliente = await pedir<Cliente>("POST", "/api/tienda/sesion", { correo, clave });
  cambiar({ id: cliente.id, nombre: cliente.nombre });
}

export async function registrarse(nombre: string, correo: string, clave: string): Promise<void> {
  const cliente = await pedir<Cliente>("POST", "/api/tienda/registro", { nombre, correo, clave });
  cambiar({ id: cliente.id, nombre: cliente.nombre });
}

export async function salir(): Promise<void> {
  try {
    await pedir<void>("DELETE", "/api/tienda/sesion");
  } catch (error) {
    // Si la sesión ya había vencido, igual se considera afuera.
    if (!(error instanceof ErrorDeApi) || error.estado !== 401) throw error;
  }
  cambiar(null);
}

/** Para cuando una llamada responde 401: la sesión venció mientras la persona estaba en la página. */
export function sesionVencida() {
  cambiar(null);
}

export function pedirLinkDeRecuperacion(correo: string): Promise<void> {
  return pedir<void>("POST", "/api/tienda/recuperar-clave", { correo });
}

export function elegirClaveNueva(token: string, clave: string): Promise<void> {
  return pedir<void>("POST", "/api/tienda/restablecer-clave", { token, clave });
}

/**
 * A dónde volver después de ingresar (?volver=/checkout). Solo rutas de esta misma tienda:
 * "//otro-sitio.com" o "https://..." se ignoran, así nadie arma un link que te mande a otra web después del login.
 */
export function destinoSeguro(volver: string | null | undefined): string {
  if (!volver || !volver.startsWith("/") || volver.startsWith("//") || volver.startsWith("/\\")) return "/mis-pedidos";
  return volver;
}
