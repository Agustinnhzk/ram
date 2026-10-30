/**
 * "Trabajá con nosotros" (ADR-010). Son formularios públicos: sin cuenta y sin token CSRF.
 * El backend manda todo por mail al taller.
 */

export type MotivoDeEmpresa = "TERCERIZAR_IT" | "ALIANZA_COMERCIAL" | "OTRO";

export type Resultado = { ok: true } | { ok: false; mensaje: string };

export const AREAS = [
  "Reparación de PC y notebooks",
  "Instalación de cámaras y alarmas",
  "Ventas y atención al cliente",
  "Otra",
];

export const MOTIVOS: { valor: MotivoDeEmpresa; titulo: string; detalle: string }[] = [
  {
    valor: "TERCERIZAR_IT",
    titulo: "Tercerizar el soporte IT",
    detalle: "Nos ocupamos de las PCs, la red, las cámaras y el mantenimiento de tu empresa.",
  },
  {
    valor: "ALIANZA_COMERCIAL",
    titulo: "Alianza comercial",
    detalle: "Sos proveedor, revendedor o tenés un negocio que se complementa con el nuestro.",
  },
  { valor: "OTRO", titulo: "Otra propuesta", detalle: "Contanos qué tenés en mente." },
];

export function postularse(formulario: FormData): Promise<Resultado> {
  return enviar("/api/publico/contactos/postulacion", { method: "POST", body: formulario });
}

export function consultarComoEmpresa(datos: Record<string, string>): Promise<Resultado> {
  return enviar("/api/publico/contactos/empresa", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(datos),
  });
}

async function enviar(ruta: string, opciones: RequestInit): Promise<Resultado> {
  try {
    const respuesta = await fetch(ruta, { ...opciones, headers: { Accept: "application/json", ...opciones.headers } });
    if (respuesta.ok) return { ok: true };
    if (respuesta.status === 413) return { ok: false, mensaje: "El archivo no puede superar los 5 MB." };
    const cuerpo = (await respuesta.json().catch(() => ({}))) as { detalle?: string };
    return { ok: false, mensaje: cuerpo.detalle ?? "No pudimos enviar tu mensaje. Probá de nuevo en unos minutos." };
  } catch {
    return { ok: false, mensaje: "No pudimos conectarnos. Revisá tu conexión y probá de nuevo." };
  }
}
