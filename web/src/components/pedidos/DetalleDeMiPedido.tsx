"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { Aviso } from "@/components/formularios";
import { IconoCheck, IconoWhatsApp } from "@/components/Iconos";
import { ErrorDeApi } from "@/lib/api-cliente";
import { sesionVencida, useSesion } from "@/lib/cuenta";
import { formatearPrecio } from "@/lib/formato";
import {
  cancelarPedido,
  ENTREGA,
  ESTADO_PARA_EL_CLIENTE,
  miPedido,
  subirComprobante,
  type DatosDePago,
  type DetalleDePedido,
} from "@/lib/pedidos";
import { linkWhatsApp } from "@/lib/whatsapp";
import { EtiquetaDeEstado } from "./EtiquetaDeEstado";
import { PedirIngreso } from "./PedirIngreso";

const TAMANO_MAXIMO = 5 * 1024 * 1024;
const fechaYHora = new Intl.DateTimeFormat("es-AR", {
  dateStyle: "short",
  timeStyle: "short",
  timeZone: "America/Argentina/Buenos_Aires",
});

export function DetalleDeMiPedido({ id, whatsapp }: { id: string; whatsapp: string }) {
  const sesion = useSesion();
  if (sesion === undefined) return <Cargando />;
  if (sesion === null) return <PedirIngreso volver={`/mis-pedidos/${id}`} mensaje="Ingresá para ver tu pedido" />;
  return <Detalle id={id} whatsapp={whatsapp} />;
}

function Detalle({ id, whatsapp }: { id: string; whatsapp: string }) {
  const esNuevo = useSearchParams().get("nuevo") === "1";
  const [pedido, setPedido] = useState<DetalleDePedido | null>(null);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(() => {
    miPedido(id)
      .then(setPedido)
      .catch((e: unknown) => {
        if (e instanceof ErrorDeApi && e.estado === 401) sesionVencida();
        setError(e instanceof Error ? e.message : "No pudimos traer el pedido.");
      });
  }, [id]);

  useEffect(cargar, [cargar]);

  if (error) {
    return (
      <div className="mt-10 space-y-4">
        <Aviso tipo="error">{error}</Aviso>
        <Link href="/mis-pedidos" className="inline-block text-oro underline underline-offset-4">
          Ver mis pedidos
        </Link>
      </div>
    );
  }
  if (!pedido) return <Cargando />;

  const estado = ESTADO_PARA_EL_CLIENTE[pedido.estado];
  const consulta = linkWhatsApp(whatsapp, `Hola, les escribo por mi pedido N° ${pedido.numero}.`);

  return (
    <div className="mt-8">
      <div className="flex flex-wrap items-center gap-3">
        <h2 className="font-display text-3xl font-bold text-principal">Pedido N° {pedido.numero}</h2>
        <EtiquetaDeEstado estado={pedido.estado} />
      </div>
      <p className="mt-1 text-sm text-suave">Hecho el {fechaYHora.format(new Date(pedido.creadoEn))}</p>

      {esNuevo && pedido.estado === "ESPERANDO_PAGO" && (
        <div className="mt-6">
          <Aviso tipo="exito">
            <span className="inline-flex items-center gap-2 font-semibold">
              <IconoCheck className="size-4 text-oro" /> ¡Recibimos tu pedido!
            </span>{" "}
            Ahora transferí el total y subí el comprobante.
          </Aviso>
        </div>
      )}

      <div className="mt-8 grid items-start gap-8 lg:grid-cols-[1fr_22rem]">
        <div className="space-y-8">
          <section className="rounded-xl border border-borde bg-superficie p-5 sm:p-6">
            <p className="font-display text-2xl font-bold text-principal italic">{estado.titulo}</p>
            <p className="mt-1 text-suave">{estado.detalle}</p>
            {pedido.estado === "CANCELADO" && pedido.motivoCancelacion && (
              <p className="mt-2 text-sm text-principal">Motivo: {pedido.motivoCancelacion}</p>
            )}

            {pedido.estado === "ESPERANDO_PAGO" && pedido.datosDePago && (
              <DatosParaTransferir datos={pedido.datosDePago} total={pedido.total} />
            )}
            {pedido.puedeSubirComprobante && <SubirComprobante id={pedido.id} alSubir={cargar} />}
          </section>

          {pedido.comprobantes.length > 0 && (
            <section>
              <h3 className="font-display text-xl font-bold text-principal italic">Comprobantes que subiste</h3>
              <ul className="mt-3 divide-y divide-borde rounded-xl border border-borde">
                {pedido.comprobantes.map((c) => (
                  <li key={c.id} className="px-4 py-3 text-sm">
                    <div className="flex flex-wrap justify-between gap-2">
                      <span className="text-principal">{c.nombre}</span>
                      <span className="text-suave">{fechaYHora.format(new Date(c.subidoEn))}</span>
                    </div>
                    {c.rechazado && (
                      <p className="mt-1 text-red-500">
                        No lo pudimos validar{c.motivoRechazo ? `: ${c.motivoRechazo}` : "."}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            </section>
          )}

          <section>
            <h3 className="font-display text-xl font-bold text-principal italic">Qué compraste</h3>
            <ul className="mt-3 divide-y divide-borde rounded-xl border border-borde">
              {pedido.lineas.map((l, i) => (
                <li key={i} className="px-4 py-3">
                  <div className="flex justify-between gap-4">
                    <span>
                      <Link href={l.referencia} className="text-principal hover:text-oro-claro">
                        {l.cantidad} × {l.descripcion}
                      </Link>
                      {l.reserva && <span className="mt-0.5 block text-xs text-aviso">Reservado: te avisamos cuando llegue</span>}
                    </span>
                    <span className="shrink-0 text-principal">{formatearPrecio(l.subtotal)}</span>
                  </div>
                  {l.detalle.length > 0 && (
                    <ul className="mt-1 space-y-0.5 text-sm text-suave">
                      {l.detalle.map((d) => (
                        <li key={d}>{d}</li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          </section>
        </div>

        <aside className="space-y-5 rounded-xl border border-borde bg-superficie p-6">
          <div>
            <p className="text-sm text-suave">Total</p>
            <p className="font-display text-4xl font-bold text-principal">{formatearPrecio(pedido.total)}</p>
            {pedido.entrega === "ENVIO_OTRA_LOCALIDAD" && <p className="text-xs text-suave">+ envío, a cotizar por WhatsApp.</p>}
          </div>
          <div className="border-t border-borde pt-4 text-sm">
            <p className="font-semibold text-principal">{ENTREGA[pedido.entrega].titulo}</p>
            {pedido.direccion && <p className="text-principal">{pedido.direccion}</p>}
            <p className="mt-1 text-suave">{ENTREGA[pedido.entrega].detalle}</p>
          </div>
          <a
            href={consulta}
            className="flex items-center justify-center gap-2 rounded-full border border-whatsapp px-5 py-2.5 text-sm font-semibold text-principal hover:bg-whatsapp/10"
          >
            <IconoWhatsApp className="size-4 text-whatsapp" />
            Consultar por WhatsApp
          </a>
          {pedido.puedeCancelar && <Cancelar id={pedido.id} alCancelar={cargar} />}
        </aside>
      </div>

      <Link href="/mis-pedidos" className="mt-10 inline-block text-sm text-suave hover:text-oro-claro">
        ← Mis pedidos
      </Link>
    </div>
  );
}

function DatosParaTransferir({ datos, total }: { datos: DatosDePago; total: number }) {
  return (
    <dl className="mt-5 grid gap-3 rounded-lg border border-oro/50 bg-fondo p-4 sm:grid-cols-2">
      <Dato titulo="Monto" valor={formatearPrecio(total)} copiar={String(total)} />
      <Dato titulo="Alias" valor={datos.alias} copiar={datos.alias} />
      <Dato titulo="A nombre de" valor={datos.titular} />
      <Dato titulo="Banco / billetera" valor={datos.entidad} />
      {datos.cbu && <Dato titulo="CBU / CVU" valor={datos.cbu} copiar={datos.cbu} />}
    </dl>
  );
}

function Dato({ titulo, valor, copiar }: { titulo: string; valor: string; copiar?: string }) {
  const [copiado, setCopiado] = useState(false);
  return (
    <div>
      <dt className="text-xs tracking-wide text-suave uppercase">{titulo}</dt>
      <dd className="flex items-center gap-2">
        <span className="font-display text-lg font-bold break-all text-principal">{valor}</span>
        {copiar && (
          <button
            type="button"
            onClick={async () => {
              try {
                await navigator.clipboard.writeText(copiar);
                setCopiado(true);
                setTimeout(() => setCopiado(false), 2000);
              } catch {
                // Sin permiso para el portapapeles: la persona lo copia a mano.
              }
            }}
            className="rounded-full border border-borde px-2.5 py-0.5 text-xs text-oro hover:border-oro"
            aria-label={`Copiar ${titulo.toLowerCase()}`}
          >
            {copiado ? "¡Copiado!" : "Copiar"}
          </button>
        )}
      </dd>
    </div>
  );
}

function SubirComprobante({ id, alSubir }: { id: string; alSubir: () => void }) {
  const entrada = useRef<HTMLInputElement>(null);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [subiendo, setSubiendo] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function subir() {
    if (!archivo) return;
    setSubiendo(true);
    setError(null);
    try {
      await subirComprobante(id, archivo);
      alSubir();
    } catch (e) {
      if (e instanceof ErrorDeApi && e.estado === 401) sesionVencida();
      setError(e instanceof Error ? e.message : "No pudimos subir el comprobante.");
      setSubiendo(false);
    }
  }

  return (
    <div className="mt-6 border-t border-borde pt-5">
      <p className="font-semibold text-principal">¿Ya transferiste? Subí el comprobante</p>
      <p className="text-sm text-suave">Una captura o el PDF que te da el banco. JPG, PNG o PDF, hasta 5 MB.</p>
      <div className="mt-3 flex flex-wrap items-center gap-3">
        <input
          ref={entrada}
          type="file"
          accept="image/jpeg,image/png,application/pdf"
          className="sr-only"
          id="comprobante"
          onChange={(e) => {
            const elegido = e.target.files?.[0] ?? null;
            setError(null);
            if (elegido && elegido.size > TAMANO_MAXIMO) {
              setError("El archivo no puede superar los 5 MB. Probá con una captura de pantalla.");
              setArchivo(null);
              return;
            }
            setArchivo(elegido);
          }}
        />
        <label htmlFor="comprobante" className="cursor-pointer rounded-full border border-borde px-5 py-2.5 text-sm text-principal hover:border-oro">
          {archivo ? "Cambiar archivo" : "Elegir archivo"}
        </label>
        {archivo && <span className="max-w-full truncate text-sm text-principal">{archivo.name}</span>}
      </div>
      {error && (
        <div className="mt-3">
          <Aviso tipo="error">{error}</Aviso>
        </div>
      )}
      <button
        type="button"
        onClick={subir}
        disabled={!archivo || subiendo}
        className="boton-oro mt-4 rounded-full px-6 py-3 font-semibold hover:brightness-110 disabled:opacity-50"
      >
        {subiendo ? "Subiendo…" : "Enviar comprobante"}
      </button>
    </div>
  );
}

function Cancelar({ id, alCancelar }: { id: string; alCancelar: () => void }) {
  const [confirmando, setConfirmando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!confirmando) {
    return (
      <button type="button" onClick={() => setConfirmando(true)} className="block w-full text-center text-sm text-suave underline underline-offset-4 hover:text-red-500">
        Cancelar pedido
      </button>
    );
  }
  return (
    <div className="rounded-lg border border-red-500/50 p-3 text-sm">
      <p className="text-principal">¿Seguro que querés cancelarlo?</p>
      {error && <p className="mt-1 text-red-500">{error}</p>}
      <div className="mt-3 flex gap-3">
        <button
          type="button"
          onClick={async () => {
            try {
              await cancelarPedido(id);
              alCancelar();
            } catch (e) {
              setError(e instanceof Error ? e.message : "No se pudo cancelar.");
            }
          }}
          className="rounded-full bg-red-500/80 px-4 py-1.5 font-semibold text-white hover:bg-red-500"
        >
          Sí, cancelar
        </button>
        <button type="button" onClick={() => setConfirmando(false)} className="text-suave hover:text-principal">
          No
        </button>
      </div>
    </div>
  );
}

function Cargando() {
  return <div aria-hidden className="mt-10 h-72 animate-pulse rounded-xl bg-superficie" />;
}
