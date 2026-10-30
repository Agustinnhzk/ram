import type { Estado } from "../api/reparaciones";
import { ESTADOS, type Tono } from "../utilidades/reparaciones";

const COLORES: Record<Tono, string> = {
  neutro: "border-borde text-suave",
  trabajo: "border-info/50 text-info",
  espera: "border-aviso/60 text-aviso",
  listo: "border-exito/60 text-exito",
  final: "border-borde bg-superficie-alta text-principal",
};

/**
 * La etiqueta de color de un estado. El color ayuda a escanear la lista, pero el texto siempre
 * dice el estado: nunca se depende solo del color.
 */
export function EtiquetaDeEstado({ estado, enGarantia }: { estado: Estado; enGarantia?: boolean }) {
  const { nombre, tono } = ESTADOS[estado];
  const texto = estado === "ENTREGADO" && enGarantia ? "En garantía" : nombre;
  return (
    <span className={`inline-block rounded-full border px-2.5 py-0.5 text-xs font-medium whitespace-nowrap ${COLORES[tono]}`}>
      {texto}
    </span>
  );
}
