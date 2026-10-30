import { useState, type FormEvent } from "react";
import { useAccionSobreOrden, type DetalleDeOrden, type Estado } from "../../api/reparaciones";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";
import { formatearPrecio, leerPrecio } from "../../utilidades/formato";
import { ACCION_HACIA, ESTADOS } from "../../utilidades/reparaciones";

/** Qué se está haciendo: pasar a un estado, o agregar una nota suelta. */
export type Accion = Estado | "NOTA";

type Props = { orden: DetalleDeOrden; accion: Accion | null; alCerrar: () => void };

/**
 * La ventana de cada acción sobre una orden. Según el paso pide cosas distintas:
 * - Presupuestar: monto y descripción.
 * - Entregar: los días de garantía de ESTA reparación (0 = sin garantía).
 * - El resto (y las notas): una nota opcional, y si la ve el cliente.
 */
export function DialogoDeAccion({ orden, accion, alCerrar }: Props) {
  const titulo = accion === null ? "" : accion === "NOTA" ? "Agregar una nota" : ACCION_HACIA[accion];
  return (
    <Dialogo abierto={accion !== null} alCerrar={alCerrar} titulo={titulo} ancho="max-w-lg">
      {accion === "PRESUPUESTADO" && <FormularioDePresupuesto orden={orden} alTerminar={alCerrar} />}
      {accion === "ENTREGADO" && <FormularioDeEntrega orden={orden} alTerminar={alCerrar} />}
      {accion !== null && accion !== "PRESUPUESTADO" && accion !== "ENTREGADO" && (
        <FormularioDeNota orden={orden} accion={accion} alTerminar={alCerrar} />
      )}
    </Dialogo>
  );
}

function FormularioDeNota({ orden, accion, alTerminar }: { orden: DetalleDeOrden; accion: Estado | "NOTA"; alTerminar: () => void }) {
  const esNota = accion === "NOTA";
  const [nota, setNota] = useState("");
  const [paraCliente, setParaCliente] = useState(false);
  const cambiarEstado = useAccionSobreOrden<{ estado: Estado; nota: string | null; notaParaCliente: boolean }>(orden.id, "estado");
  const anotar = useAccionSobreOrden<{ texto: string; paraCliente: boolean }>(orden.id, "notas");
  const avisar = useAvisar();
  const enviando = cambiarEstado.isPending || anotar.isPending;
  const error = cambiarEstado.error ?? anotar.error;

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    const listo = () => {
      avisar(esNota ? "Nota agregada" : `Orden ${orden.numero}: ${ESTADOS[accion as Estado].nombre}`);
      alTerminar();
    };
    if (esNota) anotar.mutate({ texto: nota, paraCliente }, { onSuccess: listo });
    else cambiarEstado.mutate({ estado: accion, nota: nota.trim() || null, notaParaCliente: paraCliente }, { onSuccess: listo });
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      {!esNota && (
        <p className="text-sm text-suave">
          El cliente va a ver que su equipo pasó a <strong className="text-principal">“{ESTADOS[accion].nombre}”</strong>.
        </p>
      )}
      <Campo id="nota" etiqueta={esNota ? "Nota" : "Nota"} opcional={!esNota}>
        <textarea id="nota" value={nota} onChange={(e) => setNota(e.target.value)} rows={3} autoFocus className={ESTILO_DE_CAMPO} />
      </Campo>
      <label className="flex items-start gap-3 text-sm text-principal">
        <input type="checkbox" checked={paraCliente} onChange={(e) => setParaCliente(e.target.checked)} className="mt-0.5 size-4 accent-[var(--oro)]" />
        <span>
          Que el cliente vea la nota en su seguimiento
          <span className="block text-xs text-suave">Si no, queda solo para el taller.</span>
        </span>
      </label>
      <MensajeDeError error={error} />
      <Botones enviando={enviando} deshabilitado={esNota && !nota.trim()} texto={esNota ? "Agregar nota" : ACCION_HACIA[accion]} />
    </form>
  );
}

function FormularioDePresupuesto({ orden, alTerminar }: { orden: DetalleDeOrden; alTerminar: () => void }) {
  const [monto, setMonto] = useState(orden.presupuestoMonto?.toString() ?? "");
  const [descripcion, setDescripcion] = useState(orden.presupuestoDescripcion ?? "");
  const presupuestar = useAccionSobreOrden<{ monto: number; descripcion: string }>(orden.id, "presupuesto");
  const avisar = useAvisar();
  const valor = leerPrecio(monto);

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (valor === null) return;
    presupuestar.mutate(
      { monto: valor, descripcion },
      {
        onSuccess: () => {
          avisar(`Orden ${orden.numero}: presupuesto de ${formatearPrecio(valor)}`);
          alTerminar();
        },
      },
    );
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      <Campo id="monto" etiqueta="Monto total" ayuda={valor !== null && formatearPrecio(valor)}>
        <input id="monto" inputMode="numeric" value={monto} onChange={(e) => setMonto(e.target.value)} placeholder="110000" autoFocus className={`${ESTILO_DE_CAMPO} numeros`} />
      </Campo>
      <Campo id="descripcion" etiqueta="Qué incluye" ayuda="Lo ve el cliente en su seguimiento.">
        <textarea id="descripcion" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} rows={3} placeholder="Cambio de pantalla y limpieza interna" className={ESTILO_DE_CAMPO} />
      </Campo>
      <MensajeDeError error={presupuestar.error} />
      <Botones enviando={presupuestar.isPending} deshabilitado={valor === null || !descripcion.trim()} texto="Guardar presupuesto" />
    </form>
  );
}

const GARANTIAS = [
  { dias: 0, texto: "Sin garantía" },
  { dias: 30, texto: "30 días" },
  { dias: 60, texto: "60 días" },
  { dias: 90, texto: "90 días" },
  { dias: 180, texto: "6 meses" },
];

function FormularioDeEntrega({ orden, alTerminar }: { orden: DetalleDeOrden; alTerminar: () => void }) {
  const [dias, setDias] = useState("");
  const [nota, setNota] = useState("");
  // "Hoy" se toma una sola vez, al abrir: así el cálculo no cambia entre un dibujado y otro.
  const [hoy] = useState(() => Date.now());
  const entregar = useAccionSobreOrden<{ garantiaDias: number; nota: string | null }>(orden.id, "entrega");
  const avisar = useAvisar();
  const valor = /^\d{1,4}$/.test(dias) ? Number(dias) : null;

  // Cuándo vence, para que se vea antes de confirmar. Lo que cuenta es lo que calcula el servidor.
  const vence = valor ? new Date(hoy + valor * 86_400_000).toLocaleDateString("es-AR") : null;

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (valor === null) return;
    entregar.mutate(
      { garantiaDias: valor, nota: nota.trim() || null },
      {
        onSuccess: () => {
          avisar(`Orden ${orden.numero} entregada`);
          alTerminar();
        },
      },
    );
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      <Campo id="garantia" etiqueta="Días de garantía de esta reparación" ayuda={valor === 0 ? "Sin garantía." : vence ? `Vence el ${vence}.` : "Escribí los días, o elegí uno de abajo."}>
        <input id="garantia" inputMode="numeric" value={dias} onChange={(e) => setDias(e.target.value.replace(/\D/g, ""))} autoFocus className={`${ESTILO_DE_CAMPO} numeros max-w-40`} />
      </Campo>
      <div className="flex flex-wrap gap-2" role="group" aria-label="Garantías frecuentes">
        {GARANTIAS.map((g) => (
          <button
            key={g.dias}
            type="button"
            aria-pressed={valor === g.dias}
            onClick={() => setDias(String(g.dias))}
            className={`rounded-full border px-3 py-1 text-sm ${valor === g.dias ? "border-oro bg-oro/15 text-oro-claro" : "border-borde text-principal hover:border-oro"}`}
          >
            {g.texto}
          </button>
        ))}
      </div>
      <Campo id="nota-entrega" etiqueta="Nota para el cliente" opcional>
        <textarea id="nota-entrega" value={nota} onChange={(e) => setNota(e.target.value)} rows={2} placeholder="Se cambió la bisagra izquierda" className={ESTILO_DE_CAMPO} />
      </Campo>
      <MensajeDeError error={entregar.error} />
      <Botones enviando={entregar.isPending} deshabilitado={valor === null} texto="Entregar" />
    </form>
  );
}

function MensajeDeError({ error }: { error: Error | null }) {
  if (!error) return null;
  return (
    <p role="alert" className="rounded-lg border border-peligro/60 bg-peligro/10 px-3 py-2 text-sm">
      {error.message}
    </p>
  );
}

function Botones({ enviando, deshabilitado, texto }: { enviando: boolean; deshabilitado: boolean; texto: string }) {
  return (
    <div className="flex justify-end pt-2">
      <button type="submit" disabled={enviando || deshabilitado} className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110 disabled:opacity-50">
        {enviando ? "Guardando…" : texto}
      </button>
    </div>
  );
}
