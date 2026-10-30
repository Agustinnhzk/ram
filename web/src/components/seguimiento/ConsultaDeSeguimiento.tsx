"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { IconoCheck, IconoWhatsApp } from "@/components/Iconos";
import { formatearPrecio } from "@/lib/formato";
import {
  consultarSeguimiento,
  ESTADOS_PARA_EL_CLIENTE,
  ETAPA_DE,
  ETAPAS,
  type ResultadoDeConsulta,
  type Seguimiento,
} from "@/lib/seguimiento";
import { linkWhatsApp } from "@/lib/whatsapp";

const fechaYHora = new Intl.DateTimeFormat("es-AR", {
  dateStyle: "short",
  timeStyle: "short",
  timeZone: "America/Argentina/Buenos_Aires",
});

/**
 * El formulario (N° de orden + últimos 4 del teléfono) y el resultado. Es de cliente porque
 * la consulta la hace el navegador: el número y los dígitos nunca pasan por la URL.
 */
export function ConsultaDeSeguimiento({ ordenInicial, whatsapp }: { ordenInicial: string; whatsapp: string }) {
  const [numero, setNumero] = useState(ordenInicial);
  const [digitos, setDigitos] = useState("");
  const [consultando, setConsultando] = useState(false);
  const [resultado, setResultado] = useState<ResultadoDeConsulta | null>(null);

  async function consultar(evento: FormEvent) {
    evento.preventDefault();
    setConsultando(true);
    setResultado(await consultarSeguimiento(Number(numero), digitos));
    setConsultando(false);
  }

  if (resultado?.tipo === "encontrada") {
    return (
      <Resultado
        seguimiento={resultado.seguimiento}
        whatsapp={whatsapp}
        alVolver={() => {
          setResultado(null);
          setDigitos("");
        }}
      />
    );
  }

  return (
    <form onSubmit={consultar} className="mt-8 max-w-md rounded-2xl border border-borde bg-superficie p-6" noValidate>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor="orden" className="text-sm text-suave">
            N° de orden
          </label>
          <input
            id="orden"
            inputMode="numeric"
            autoComplete="off"
            value={numero}
            onChange={(e) => setNumero(e.target.value.replace(/\D/g, "").slice(0, 7))}
            className="numeros mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 font-display text-xl font-bold text-principal focus:border-oro focus:outline-none"
          />
        </div>
        <div>
          <label htmlFor="digitos" className="text-sm text-suave">
            Últimos 4 números de tu teléfono
          </label>
          <input
            id="digitos"
            inputMode="numeric"
            autoComplete="off"
            value={digitos}
            onChange={(e) => setDigitos(e.target.value.replace(/\D/g, "").slice(0, 4))}
            placeholder="1234"
            className="numeros mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 font-display text-xl font-bold tracking-[0.3em] text-principal focus:border-oro focus:outline-none"
          />
        </div>
      </div>

      <div aria-live="polite">
        {resultado && (
          <p role="alert" className="mt-4 rounded-lg border border-oro/50 bg-oro/10 px-3 py-2 text-sm text-principal">
            {resultado.tipo === "no-encontrada" && "No encontramos una reparación con esos datos. Revisá el número de orden y los 4 números."}
            {resultado.tipo === "demasiados-intentos" && resultado.mensaje}
            {resultado.tipo === "error" && "No pudimos consultar en este momento. Probá de nuevo en un rato o escribinos por WhatsApp."}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={consultando || !numero || digitos.length !== 4}
        className="boton-oro mt-5 w-full rounded-full py-3 font-semibold hover:brightness-110 disabled:opacity-50"
      >
        {consultando ? "Buscando…" : "Ver cómo va"}
      </button>
      <p className="mt-3 text-center text-xs text-suave">El número de orden está en tu comprobante y en el WhatsApp que te mandamos.</p>
    </form>
  );
}

function Resultado(props: { seguimiento: Seguimiento; whatsapp: string; alVolver: () => void }) {
  const s = props.seguimiento;
  const encabezado = useRef<HTMLHeadingElement>(null);

  // Al aparecer, el foco va al título: quien usa lector de pantalla escucha enseguida cómo va su equipo.
  useEffect(() => {
    encabezado.current?.focus();
  }, []);

  const { titulo, detalle } = ESTADOS_PARA_EL_CLIENTE[s.estado];
  const etapa = ETAPA_DE[s.estado];
  const listo = s.estado === "LISTO_PARA_RETIRAR";
  // La línea de tiempo, de lo más nuevo a lo más viejo.
  const pasos = [...s.pasos].reverse();

  return (
    <div className="mt-8 space-y-6">
      <section className={`rounded-2xl border p-6 sm:p-8 ${listo ? "border-oro bg-oro/10" : "border-borde bg-superficie"}`}>
        <p className="text-sm text-suave">
          Orden <span className="numeros font-semibold text-principal">{s.numero}</span> · {s.equipo}
        </p>
        <h2 ref={encabezado} tabIndex={-1} className="mt-2 text-3xl text-principal focus:outline-none sm:text-4xl">
          {titulo}
        </h2>
        <p className="mt-2 text-lg text-suave">{detalle}</p>

        {s.estado === "ENTREGADO" && (
          <p className="mt-3 text-principal">
            {s.garantiaHasta
              ? s.enGarantia
                ? `Tiene garantía hasta el ${dia(s.garantiaHasta)}.`
                : `La garantía venció el ${dia(s.garantiaHasta)}.`
              : "Esta reparación no tiene garantía."}
          </p>
        )}

        {/* La barra de etapas: dónde está y cuánto falta. */}
        <ol className="mt-6 grid grid-cols-6 gap-1.5" aria-label="Etapas de la reparación">
          {ETAPAS.map((nombre, i) => (
            <li key={nombre} aria-current={i === etapa ? "step" : undefined}>
              <div className={`h-1.5 rounded-full ${i <= etapa ? "bg-oro" : "bg-borde"}`} />
              {/* En el celular no entran 6 nombres: quedan para lectores de pantalla y se muestra la línea de abajo. */}
              <span className={`sr-only sm:not-sr-only sm:mt-1.5 sm:block sm:text-xs ${i === etapa ? "font-semibold text-oro-claro" : "text-suave"}`}>
                {nombre}
              </span>
            </li>
          ))}
        </ol>
        <p className="mt-2 text-sm text-suave sm:hidden" aria-hidden>
          Etapa {etapa + 1} de {ETAPAS.length} · <span className="font-semibold text-oro-claro">{ETAPAS[etapa]}</span>
        </p>
      </section>

      {s.presupuestoMonto !== null && (
        <section className="rounded-2xl border border-borde bg-superficie p-6">
          <h2 className="text-2xl text-oro-claro">Presupuesto</h2>
          <p className="numeros mt-2 font-display text-3xl font-bold text-principal">{formatearPrecio(s.presupuestoMonto)}</p>
          {s.presupuestoDescripcion && <p className="mt-1 whitespace-pre-line text-principal">{s.presupuestoDescripcion}</p>}
          {s.estado === "PRESUPUESTADO" && (
            <a
              href={linkWhatsApp(props.whatsapp, `Hola, apruebo el presupuesto de la orden ${s.numero} (${s.equipo}).`)}
              className="mt-4 inline-flex items-center gap-2 rounded-full bg-whatsapp px-5 py-2.5 font-semibold text-negro hover:brightness-110"
            >
              <IconoWhatsApp className="size-5" />
              Aprobar por WhatsApp
            </a>
          )}
        </section>
      )}

      <section className="rounded-2xl border border-borde bg-superficie p-6">
        <h2 className="text-2xl text-oro-claro">Qué fue pasando</h2>
        <ol className="mt-4 space-y-4 border-l border-borde pl-5">
          {pasos.map((paso, i) => (
            <li key={i} className="relative">
              <span aria-hidden className={`absolute top-1.5 -left-[1.6rem] size-2.5 rounded-full ${i === 0 ? "bg-oro" : "bg-borde"}`} />
              {paso.estado && <p className="font-semibold text-principal">{ESTADOS_PARA_EL_CLIENTE[paso.estado].titulo}</p>}
              {paso.nota && <p className={`whitespace-pre-line ${paso.estado ? "text-suave" : "text-principal"}`}>{paso.nota}</p>}
              <p className="text-xs text-suave">{fechaYHora.format(new Date(paso.cuando))}</p>
            </li>
          ))}
        </ol>
      </section>

      <div className="flex flex-wrap gap-3">
        <a
          href={linkWhatsApp(props.whatsapp, `Hola, quería consultar por la orden ${s.numero}.`)}
          className="inline-flex items-center gap-2 rounded-full border border-borde px-5 py-2.5 text-principal hover:border-oro"
        >
          <IconoWhatsApp className="size-5 text-whatsapp" />
          ¿Dudas? Escribinos
        </a>
        <button type="button" onClick={props.alVolver} className="rounded-full px-5 py-2.5 text-oro underline underline-offset-4 hover:text-oro-claro">
          Consultar otra orden
        </button>
      </div>

      <p className="flex items-center gap-2 text-xs text-suave">
        <IconoCheck className="size-4 text-oro" />
        Esta página se actualiza cuando la volvés a consultar.
      </p>
    </div>
  );
}

/** "2026-12-28" → "28/12/2026", sin pasar por Date (que podría correrlo un día por la zona horaria). */
function dia(iso: string): string {
  const [anio, mes, d] = iso.split("-");
  return `${d}/${mes}/${anio}`;
}
