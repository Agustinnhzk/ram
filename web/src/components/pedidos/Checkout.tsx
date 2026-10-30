"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Aviso, AreaDeTexto, BotonPrincipal, Campo } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { totalDelCarrito, useCarrito, vaciarCarrito, type ItemCarrito } from "@/lib/carrito";
import { sesionVencida, useSesion } from "@/lib/cuenta";
import { formatearPrecio } from "@/lib/formato";
import {
  cotizar,
  ENTREGA,
  itemParaElPedido,
  realizarPedido,
  type Cotizacion,
  type FormaDeEntrega,
  type ItemParaElPedido,
} from "@/lib/pedidos";
import { PedirIngreso } from "./PedirIngreso";

const FORMAS: FormaDeEntrega[] = ["RETIRO", "ENVIO_ROSARIO", "ENVIO_OTRA_LOCALIDAD"];
const SIN_ITEMS: ItemCarrito[] = [];

/**
 * El checkout: cómo lo recibe, un teléfono, y el resumen con los precios QUE CALCULA EL SERVIDOR
 * (el carrito del navegador es solo una guía). Al confirmar se crea el pedido y se va a su página,
 * donde están el alias y la carga del comprobante.
 */
export function Checkout() {
  const sesion = useSesion();
  const carrito = useCarrito();

  if (sesion === undefined || carrito === null) return <div aria-hidden className="mt-10 h-72 animate-pulse rounded-xl bg-superficie" />;
  if (sesion === null) return <PedirIngreso volver="/checkout" mensaje="Para comprar, ingresá a tu cuenta" />;
  if (carrito.length === 0) {
    return (
      <div className="mt-10">
        <Aviso tipo="info">Tu carrito está vacío.</Aviso>
        <Link href="/productos" className="mt-4 inline-block text-oro underline underline-offset-4">
          Ver productos
        </Link>
      </div>
    );
  }
  return <FormularioDeCompra />;
}

function FormularioDeCompra() {
  const router = useRouter();
  const carrito = useCarrito() ?? SIN_ITEMS;
  const items = useMemo(() => carrito.map(itemParaElPedido), [carrito]);
  const validos = useMemo(() => items.filter((i): i is ItemParaElPedido => i !== null), [items]);
  const hayIlegibles = validos.length < items.length;

  const [entrega, setEntrega] = useState<FormaDeEntrega>("RETIRO");
  const [direccion, setDireccion] = useState("");
  const [telefono, setTelefono] = useState("");
  const [nota, setNota] = useState("");
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [errorDeCotizacion, setErrorDeCotizacion] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  // Precios reales: se piden al servidor cada vez que cambia el carrito. La entrega no cambia el total
  // (el envío en Rosario es gratis y el de afuera se cotiza aparte), por eso se cotiza siempre "con retiro".
  useEffect(() => {
    let vigente = true;
    cotizar({ items: validos, entrega: "RETIRO", telefono: "" })
      .then((c) => {
        if (!vigente) return;
        setCotizacion(c);
        setErrorDeCotizacion(null);
      })
      .catch((e: unknown) => {
        if (!vigente) return;
        if (e instanceof ErrorDeApi && e.estado === 401) sesionVencida();
        setCotizacion(null);
        setErrorDeCotizacion(e instanceof Error ? e.message : "No pudimos calcular el total.");
      });
    return () => {
      vigente = false;
    };
  }, [validos]);

  async function confirmar(evento: FormEvent) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      const pedido = await realizarPedido({
        items: validos,
        entrega,
        direccion: entrega === "RETIRO" ? undefined : direccion,
        telefono,
        nota: nota || undefined,
      });
      vaciarCarrito();
      router.push(`/mis-pedidos/${pedido.id}?nuevo=1`);
    } catch (e) {
      if (e instanceof ErrorDeApi && e.estado === 401) sesionVencida();
      setError(e instanceof Error ? e.message : "No pudimos confirmar el pedido.");
      setEnviando(false);
    }
  }

  const totalDelNavegador = totalDelCarrito(carrito);
  const preciosCambiaron = cotizacion !== null && cotizacion.total !== totalDelNavegador;

  return (
    <form onSubmit={confirmar} className="mt-10 grid items-start gap-8 lg:grid-cols-[1fr_24rem]">
      <div className="space-y-8">
        <fieldset>
          <legend className="font-display text-2xl font-bold text-principal italic">1. ¿Cómo lo recibís?</legend>
          <div className="mt-4 grid gap-3">
            {FORMAS.map((forma) => (
              <label
                key={forma}
                className={`flex cursor-pointer gap-3 rounded-xl border p-4 transition-colors ${
                  entrega === forma ? "border-oro bg-oro/10" : "border-borde bg-superficie hover:border-oro/60"
                }`}
              >
                <input
                  type="radio"
                  name="entrega"
                  value={forma}
                  checked={entrega === forma}
                  onChange={() => setEntrega(forma)}
                  className="mt-1 accent-[var(--oro)]"
                />
                <span>
                  <span className="block font-semibold text-principal">{ENTREGA[forma].titulo}</span>
                  <span className="block text-sm text-suave">{ENTREGA[forma].detalle}</span>
                </span>
              </label>
            ))}
          </div>
          {entrega !== "RETIRO" && (
            <Campo
              className="mt-4"
              etiqueta={entrega === "ENVIO_ROSARIO" ? "Dirección en Rosario" : "Dirección y localidad"}
              placeholder={entrega === "ENVIO_ROSARIO" ? "Córdoba 1234, 2° B" : "San Martín 100, Funes"}
              autoComplete="street-address"
              required
              maxLength={300}
              value={direccion}
              onChange={(e) => setDireccion(e.target.value)}
            />
          )}
        </fieldset>

        <fieldset>
          <legend className="font-display text-2xl font-bold text-principal italic">2. Tus datos de contacto</legend>
          <div className="mt-4 space-y-4">
            <Campo
              etiqueta="Teléfono / WhatsApp"
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              placeholder="341 555-1234"
              required
              maxLength={30}
              ayuda="Te escribimos por acá para coordinar la entrega."
              value={telefono}
              onChange={(e) => setTelefono(e.target.value)}
            />
            <AreaDeTexto
              etiqueta="Nota (opcional)"
              placeholder="Horario en que estás, referencias de la dirección…"
              maxLength={500}
              rows={3}
              value={nota}
              onChange={(e) => setNota(e.target.value)}
            />
          </div>
        </fieldset>

        <div>
          <p className="font-display text-2xl font-bold text-principal italic">3. Pagás por transferencia</p>
          <p className="mt-2 text-suave">
            Al confirmar te mostramos el alias. Transferís el total y subís el comprobante: lo revisamos y te avisamos.
          </p>
        </div>
      </div>

      <aside className="rounded-xl border border-borde bg-superficie p-6 lg:sticky lg:top-36">
        <h2 className="font-sans text-base font-semibold text-suave not-italic">Tu pedido</h2>

        {hayIlegibles && (
          <div className="mt-4">
            <Aviso tipo="error">
              Algo de tu carrito no se puede comprar así. <Link href="/carrito" className="underline">Revisalo</Link> y volvé a agregarlo.
            </Aviso>
          </div>
        )}

        {errorDeCotizacion ? (
          <div className="mt-4">
            <Aviso tipo="error">
              {errorDeCotizacion}{" "}
              <Link href="/carrito" className="underline">
                Volver al carrito
              </Link>
            </Aviso>
          </div>
        ) : !cotizacion ? (
          <div aria-hidden className="mt-4 h-40 animate-pulse rounded-lg bg-fondo/60" />
        ) : (
          <>
            <ul className="mt-4 space-y-3 border-b border-borde pb-4">
              {cotizacion.lineas.map((l, i) => (
                <li key={i} className="flex justify-between gap-4 text-sm">
                  <span className="text-principal">
                    {l.cantidad} × {l.descripcion}
                    {l.reserva && <span className="mt-0.5 block text-xs text-aviso">Sin stock: queda reservado y te avisamos cuando llegue</span>}
                  </span>
                  <span className="shrink-0 text-principal">{formatearPrecio(l.subtotal)}</span>
                </li>
              ))}
            </ul>
            <dl className="mt-4 space-y-1 text-sm">
              <div className="flex justify-between">
                <dt className="text-suave">Envío</dt>
                <dd className="text-principal">
                  {entrega === "RETIRO" ? "Retirás en el local" : entrega === "ENVIO_ROSARIO" ? "Gratis" : "A cotizar por WhatsApp"}
                </dd>
              </div>
            </dl>
            <p className="mt-4 text-sm text-suave">Total a transferir</p>
            <p className="font-display text-4xl font-bold text-principal">{formatearPrecio(cotizacion.total)}</p>
            {entrega === "ENVIO_OTRA_LOCALIDAD" && <p className="mt-1 text-xs text-suave">+ el envío, que se paga aparte.</p>}
            {preciosCambiaron && (
              <p className="mt-3 text-xs text-oro">Algunos precios se actualizaron desde que los agregaste al carrito.</p>
            )}
          </>
        )}

        {error && (
          <div className="mt-4">
            <Aviso tipo="error">{error}</Aviso>
          </div>
        )}

        <BotonPrincipal enviando={enviando} disabled={!cotizacion || hayIlegibles} className="mt-6 w-full">
          Confirmar pedido
        </BotonPrincipal>
        <Link href="/carrito" className="mt-3 block text-center text-sm text-suave hover:text-oro-claro">
          ← Volver al carrito
        </Link>
      </aside>
    </form>
  );
}
