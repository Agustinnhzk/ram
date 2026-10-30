/**
 * La única puerta entre el sistema y la API de Spring. Todas las llamadas pasan por pedir().
 *
 * Se ocupa de tres cosas, para que ninguna pantalla tenga que pensar en ellas:
 * 1. La sesión: la cookie SESSION la manda el navegador solo (mismo dominio, ADR-007).
 * 2. El CSRF: todo pedido que modifica algo (POST, PUT, DELETE) lleva el token de la cookie
 *    XSRF-TOKEN en el header X-XSRF-TOKEN (ADR-006). Si todavía no hay token, lo pide primero.
 * 3. Los errores: cualquier respuesta que no sea 2xx se convierte en un ErrorDeApi con el estado
 *    y el motivo que manda el backend ({"error": "sin_permiso", "detalle": "..."}).
 */

export class ErrorDeApi extends Error {
  readonly estado: number;
  /** El código del backend: "credenciales_invalidas", "sin_permiso", "datos_invalidos"... */
  readonly codigo: string | undefined;

  constructor(estado: number, codigo: string | undefined, detalle: string | undefined) {
    super(detalle ?? mensajePorEstado(estado));
    this.name = "ErrorDeApi";
    this.estado = estado;
    this.codigo = codigo;
  }

  get sinSesion(): boolean {
    return this.estado === 401;
  }
}

type Metodo = "GET" | "POST" | "PUT" | "DELETE";

export async function pedir<T>(metodo: Metodo, ruta: string, cuerpo?: unknown): Promise<T> {
  const respuesta = await enviar(metodo, ruta, cuerpo);

  // Un 403 SIN cuerpo JSON es Spring rechazando el token CSRF (por ejemplo, si venció).
  // Se pide uno nuevo y se reintenta UNA vez. Un 403 con {"error": ...} es "no tenés permiso": no se reintenta.
  if (respuesta.status === 403 && metodo !== "GET" && !(await tieneCodigoDeError(respuesta.clone()))) {
    await pedirTokenCsrf();
    return interpretar<T>(await enviar(metodo, ruta, cuerpo));
  }
  return interpretar<T>(respuesta);
}

/**
 * Un formulario con archivos (fotos de productos, banners). El Content-Type no se pone a mano: el navegador
 * arma el "multipart" con su separador. Mismo CSRF y mismo reintento que pedir().
 */
export async function enviarFormulario<T>(metodo: "POST" | "PUT", ruta: string, formulario: FormData): Promise<T> {
  const mandar = async () =>
    fetch(ruta, {
      method: metodo,
      headers: { Accept: "application/json", "X-XSRF-TOKEN": tokenCsrf() ?? (await pedirTokenCsrf()) },
      body: formulario,
      credentials: "same-origin",
    });
  const respuesta = await mandar();
  if (respuesta.status === 403 && !(await tieneCodigoDeError(respuesta.clone()))) {
    await pedirTokenCsrf();
    return interpretar<T>(await mandar());
  }
  return interpretar<T>(respuesta);
}

async function enviar(metodo: Metodo, ruta: string, cuerpo: unknown): Promise<Response> {
  const encabezados: Record<string, string> = { Accept: "application/json" };
  if (cuerpo !== undefined) encabezados["Content-Type"] = "application/json";
  if (metodo !== "GET") encabezados["X-XSRF-TOKEN"] = tokenCsrf() ?? (await pedirTokenCsrf());

  return fetch(ruta, {
    method: metodo,
    headers: encabezados,
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    credentials: "same-origin",
  });
}

async function interpretar<T>(respuesta: Response): Promise<T> {
  if (respuesta.ok) {
    // 204 No Content (por ejemplo, al cambiar un precio): no hay nada que leer.
    return (respuesta.status === 204 ? undefined : await respuesta.json()) as T;
  }
  const error = await leerError(respuesta);
  throw new ErrorDeApi(respuesta.status, error.codigo, error.detalle);
}

async function leerError(respuesta: Response): Promise<{ codigo?: string; detalle?: string }> {
  try {
    const cuerpo = (await respuesta.json()) as { error?: unknown; detalle?: unknown };
    return {
      codigo: typeof cuerpo.error === "string" ? cuerpo.error : undefined,
      detalle: typeof cuerpo.detalle === "string" ? cuerpo.detalle : undefined,
    };
  } catch {
    return {};
  }
}

async function tieneCodigoDeError(respuesta: Response): Promise<boolean> {
  return (await leerError(respuesta)).codigo !== undefined;
}

/** El token que Spring dejó en la cookie XSRF-TOKEN (no es HttpOnly justamente para que lo podamos leer). */
function tokenCsrf(): string | undefined {
  const cookie = document.cookie.split("; ").find((c) => c.startsWith("XSRF-TOKEN="));
  return cookie ? decodeURIComponent(cookie.slice("XSRF-TOKEN=".length)) : undefined;
}

async function pedirTokenCsrf(): Promise<string> {
  const respuesta = await fetch("/api/publico/csrf", { credentials: "same-origin" });
  if (!respuesta.ok) throw new ErrorDeApi(respuesta.status, undefined, "No se pudo conectar con el servidor.");
  const { token } = (await respuesta.json()) as { token: string };
  return token;
}

function mensajePorEstado(estado: number): string {
  if (estado === 401) return "Tu sesión terminó. Volvé a ingresar.";
  if (estado === 403) return "No tenés permiso para hacer esto.";
  if (estado === 404) return "No se encontró lo que buscabas.";
  if (estado === 413) return "El archivo es demasiado grande.";
  if (estado >= 500) return "El servidor tuvo un problema. Probá de nuevo en un rato.";
  return "Algo salió mal. Probá de nuevo.";
}
