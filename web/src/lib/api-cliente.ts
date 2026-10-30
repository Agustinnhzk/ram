/**
 * Las llamadas a la API que hace el NAVEGADOR (login, pedidos, formularios). Pasan por el proxy de /api
 * (next.config.ts), así que van al mismo dominio: la cookie de sesión viaja sola.
 *
 * Igual que en el sistema de gestión (ADR-006), todo lo que modifica algo lleva el token CSRF:
 * Spring lo deja en la cookie XSRF-TOKEN y acá se copia al encabezado X-XSRF-TOKEN.
 * Otra web no puede leer esa cookie, así que no puede hacer compras "en nombre" del cliente.
 */

export class ErrorDeApi extends Error {
  readonly estado: number;
  /** El código del backend: "credenciales_invalidas", "producto_no_disponible"... */
  readonly codigo: string | undefined;

  constructor(estado: number, codigo: string | undefined, detalle: string | undefined) {
    super(detalle ?? mensajePorEstado(estado, codigo));
    this.name = "ErrorDeApi";
    this.estado = estado;
    this.codigo = codigo;
  }
}

type Metodo = "GET" | "POST" | "PUT" | "DELETE";

/** JSON de ida y vuelta. */
export function pedir<T>(metodo: Metodo, ruta: string, cuerpo?: unknown): Promise<T> {
  return conReintentoCsrf<T>(metodo, async () => {
    const encabezados: Record<string, string> = { Accept: "application/json" };
    if (cuerpo !== undefined) encabezados["Content-Type"] = "application/json";
    if (metodo !== "GET") encabezados["X-XSRF-TOKEN"] = await token();
    return fetch(ruta, {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
      credentials: "same-origin",
    });
  });
}

/**
 * Un formulario con archivos (comprobante, CV). El Content-Type NO se pone a mano: el navegador lo arma
 * con el "boundary" que separa cada parte.
 */
export function enviarFormulario<T>(ruta: string, formulario: FormData): Promise<T> {
  return conReintentoCsrf<T>("POST", async () =>
    fetch(ruta, {
      method: "POST",
      headers: { Accept: "application/json", "X-XSRF-TOKEN": await token() },
      body: formulario,
      credentials: "same-origin",
    }),
  );
}

/**
 * Un 403 SIN cuerpo JSON es Spring rechazando el token CSRF (por ejemplo, porque cambió al iniciar sesión):
 * se pide uno nuevo y se reintenta UNA vez. Un 403 con {"error": ...} es "no tenés permiso": no se reintenta.
 */
async function conReintentoCsrf<T>(metodo: Metodo, enviar: () => Promise<Response>): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await enviar();
    if (respuesta.status === 403 && metodo !== "GET" && (await leerError(respuesta.clone())).codigo === undefined) {
      await pedirTokenCsrf();
      respuesta = await enviar();
    }
  } catch (error) {
    if (error instanceof ErrorDeApi) throw error;
    throw new ErrorDeApi(0, "sin_conexion", "No pudimos conectarnos. Revisá tu conexión y probá de nuevo.");
  }
  if (respuesta.ok) {
    const texto = await respuesta.text();
    return (texto ? JSON.parse(texto) : undefined) as T;
  }
  const { codigo, detalle } = await leerError(respuesta);
  throw new ErrorDeApi(respuesta.status, codigo, detalle);
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

async function token(): Promise<string> {
  const cookie = document.cookie.split("; ").find((c) => c.startsWith("XSRF-TOKEN="));
  return cookie ? decodeURIComponent(cookie.slice("XSRF-TOKEN=".length)) : pedirTokenCsrf();
}

async function pedirTokenCsrf(): Promise<string> {
  const respuesta = await fetch("/api/publico/csrf", { credentials: "same-origin" });
  if (!respuesta.ok) throw new ErrorDeApi(respuesta.status, undefined, "No pudimos conectarnos con el servidor.");
  const { token } = (await respuesta.json()) as { token: string };
  return token;
}

function mensajePorEstado(estado: number, codigo: string | undefined): string {
  if (codigo === "credenciales_invalidas") return "El correo o la contraseña no son correctos.";
  if (codigo === "correo_ya_registrado") return "Ya hay una cuenta con ese correo. ¿Querés ingresar?";
  if (codigo === "enlace_invalido") return "El link no es válido o ya venció. Pedí uno nuevo.";
  if (codigo === "pedido_no_encontrado") return "No encontramos ese pedido.";
  if (estado === 401) return "Tenés que ingresar a tu cuenta.";
  if (estado === 403) return "No tenés permiso para hacer esto.";
  if (estado === 413) return "El archivo no puede superar los 5 MB.";
  if (estado === 429) return "Hiciste muchos intentos seguidos. Probá de nuevo en un rato.";
  if (estado >= 500) return "Tuvimos un problema. Probá de nuevo en un rato.";
  return "Algo salió mal. Probá de nuevo.";
}
