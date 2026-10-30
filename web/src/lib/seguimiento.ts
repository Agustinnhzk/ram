import { pedir } from "./api-cliente";

/**
 * El seguimiento de reparaciones que ve el cliente (ADR-009): la forma del JSON de
 * POST /api/publico/seguimiento y cómo se explica cada estado en su idioma.
 */

export type EstadoReparacion =
  | "INGRESADO"
  | "EN_DIAGNOSTICO"
  | "PRESUPUESTADO"
  | "APROBADO"
  | "RECHAZADO"
  | "EN_REPARACION"
  | "ESPERANDO_REPUESTO"
  | "EN_PROVEEDOR"
  | "SIN_REPARACION"
  | "LISTO_PARA_RETIRAR"
  | "ENTREGADO";

export type Seguimiento = {
  numero: number;
  equipo: string;
  estado: EstadoReparacion;
  presupuestoMonto: number | null;
  presupuestoDescripcion: string | null;
  ingresadoEn: string;
  /** "2026-12-28" */
  garantiaHasta: string | null;
  enGarantia: boolean;
  /** Del más viejo al más nuevo. estado null = un aviso del taller. */
  pasos: { estado: EstadoReparacion | null; nota: string | null; cuando: string }[];
};

export type ResultadoDeConsulta =
  | { tipo: "encontrada"; seguimiento: Seguimiento }
  | { tipo: "no-encontrada" }
  | { tipo: "demasiados-intentos"; mensaje: string }
  | { tipo: "error" };

/**
 * Consulta al backend (a través del proxy de /api, ver next.config.ts). Es POST para que los
 * dígitos del teléfono no queden en la URL. Cualquier fallo vuelve como un resultado, no como excepción.
 */
export async function consultarSeguimiento(numero: number, telefono: string): Promise<ResultadoDeConsulta> {
  try {
    const respuesta = await fetch("/api/publico/seguimiento", {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ numero, telefono }),
    });
    if (respuesta.ok) return { tipo: "encontrada", seguimiento: (await respuesta.json()) as Seguimiento };
    if (respuesta.status === 404) return { tipo: "no-encontrada" };
    if (respuesta.status === 429) {
      const cuerpo = (await respuesta.json().catch(() => ({}))) as { detalle?: string };
      return { tipo: "demasiados-intentos", mensaje: cuerpo.detalle ?? "Hiciste muchos intentos. Probá de nuevo en un rato." };
    }
    return { tipo: "error" };
  } catch {
    return { tipo: "error" };
  }
}

/**
 * Las reparaciones del cliente logueado (ADR-014): las de la ficha del taller vinculada a su cuenta.
 * Vacío si todavía no está vinculada (pasa sola con su primer pedido, o la vincula el taller).
 */
export const misReparaciones = () => pedir<Seguimiento[]>("GET", "/api/tienda/reparaciones");

/** Cada estado, contado para el cliente: un título corto y qué significa para él. */
export const ESTADOS_PARA_EL_CLIENTE: Record<EstadoReparacion, { titulo: string; detalle: string }> = {
  INGRESADO: { titulo: "Recibimos tu equipo", detalle: "Ya está en el taller. En breve empezamos a revisarlo." },
  EN_DIAGNOSTICO: { titulo: "Lo estamos revisando", detalle: "Estamos viendo qué tiene para pasarte un presupuesto." },
  PRESUPUESTADO: { titulo: "Tu presupuesto está listo", detalle: "Lo reparamos solo si lo aprobás. Respondenos por WhatsApp." },
  APROBADO: { titulo: "Presupuesto aprobado", detalle: "¡Gracias! Ya está en la fila para repararse." },
  RECHAZADO: { titulo: "No lo vamos a reparar", detalle: "No aprobaste el presupuesto. Te avisamos cuando puedas retirarlo." },
  EN_REPARACION: { titulo: "Lo estamos reparando", detalle: "Estamos trabajando en tu equipo." },
  ESPERANDO_REPUESTO: { titulo: "Esperando un repuesto", detalle: "Pedimos un repuesto y seguimos apenas llegue." },
  EN_PROVEEDOR: { titulo: "En un service especializado", detalle: "Lo enviamos a un servicio técnico especializado. Te avisamos cuando vuelva." },
  SIN_REPARACION: { titulo: "No se pudo reparar", detalle: "Hicimos todo lo posible. No tiene costo. Te avisamos cuando puedas retirarlo." },
  LISTO_PARA_RETIRAR: { titulo: "¡Listo para retirar!", detalle: "Ya podés pasar a buscarlo." },
  ENTREGADO: { titulo: "Entregado", detalle: "Ya lo retiraste. ¡Gracias por confiar en nosotros!" },
};

/** Las etapas de la barra de progreso, y en cuál está cada estado. */
export const ETAPAS = ["Recibido", "Revisión", "Presupuesto", "Reparación", "Listo", "Entregado"] as const;

export const ETAPA_DE: Record<EstadoReparacion, number> = {
  INGRESADO: 0,
  EN_DIAGNOSTICO: 1,
  PRESUPUESTADO: 2,
  APROBADO: 3,
  EN_REPARACION: 3,
  ESPERANDO_REPUESTO: 3,
  EN_PROVEEDOR: 3,
  RECHAZADO: 4,
  SIN_REPARACION: 4,
  LISTO_PARA_RETIRAR: 4,
  ENTREGADO: 5,
};
