"use client";

import Image from "next/image";
import Link from "next/link";
import {
  cambiarCantidad,
  CANTIDAD_MAXIMA,
  mensajeDelPedido,
  quitarDelCarrito,
  totalDelCarrito,
  useCarrito,
  vaciarCarrito,
  type ItemCarrito,
} from "@/lib/carrito";
import { formatearPrecio } from "@/lib/formato";
import { esImagenDeLaApi } from "@/lib/imagenes";
import { linkWhatsApp } from "@/lib/whatsapp";
import { IconoPc, IconoWhatsApp } from "./Iconos";

/** El contenido de /carrito. De cliente, porque el carrito vive en el navegador. */
export function Carrito({ whatsapp }: { whatsapp: string }) {
  const items = useCarrito();

  // null: el navegador todavía no leyó el carrito (dura un instante).
  if (items === null) return <div aria-hidden className="mt-10 h-48 animate-pulse rounded-xl bg-superficie" />;

  if (items.length === 0) {
    return (
      <div className="mt-10 rounded-xl border border-borde bg-superficie px-6 py-14 text-center">
        <p className="font-display text-2xl font-bold italic text-principal">Tu carrito está vacío</p>
        <p className="mt-2 text-suave">Mirá los productos o armá tu PC a medida.</p>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Link href="/productos" className="boton-oro rounded-full px-6 py-2.5 font-semibold">
            Ver productos
          </Link>
          <Link href="/arma-tu-pc" className="rounded-full border border-borde px-6 py-2.5 text-principal hover:border-oro">
            Armá tu PC
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="mt-10 grid items-start gap-8 lg:grid-cols-[1fr_22rem]">
      <section aria-label="Productos en el carrito">
        <ul className="divide-y divide-borde border-y border-borde">
          {items.map((item) => (
            <Fila key={item.id} item={item} />
          ))}
        </ul>
        <button type="button" onClick={vaciarCarrito} className="mt-4 text-sm text-suave underline underline-offset-4 hover:text-oro-claro">
          Vaciar carrito
        </button>
      </section>

      <aside className="rounded-xl border border-borde bg-superficie p-6 lg:sticky lg:top-36">
        <h2 className="font-sans text-base font-semibold not-italic text-suave">Total</h2>
        <p className="mt-1 font-display text-4xl font-bold text-principal">{formatearPrecio(totalDelCarrito(items))}</p>
        <p className="mt-4 text-sm text-suave">
          Pagás por transferencia y subís el comprobante. Retirás en el local, o te lo llevamos (gratis en Rosario).
        </p>
        <Link
          href="/checkout"
          className="boton-oro mt-5 flex items-center justify-center rounded-full px-6 py-3 font-semibold hover:brightness-110"
        >
          Continuar compra
        </Link>
        <a
          href={linkWhatsApp(whatsapp, mensajeDelPedido(items))}
          className="mt-3 flex items-center justify-center gap-2 rounded-full border border-whatsapp px-6 py-2.5 text-sm font-semibold text-principal hover:bg-whatsapp/10"
        >
          <IconoWhatsApp className="size-4 text-whatsapp" />
          Prefiero pedirlo por WhatsApp
        </a>
        <Link href="/productos" className="mt-3 block text-center text-sm text-oro underline underline-offset-4 hover:text-oro-claro">
          Seguir comprando
        </Link>
      </aside>
    </div>
  );
}

function Fila({ item }: { item: ItemCarrito }) {
  return (
    <li className="flex gap-4 py-5">
      <div className="grid size-20 shrink-0 place-items-center overflow-hidden rounded-lg border border-borde bg-superficie sm:size-24">
        {item.imagen ? (
          <Image src={item.imagen} alt="" width={96} height={96} unoptimized={esImagenDeLaApi(item.imagen)} className="size-full object-cover" />
        ) : (
          <IconoPc className="size-10 text-oro" />
        )}
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-start justify-between gap-x-4 gap-y-1">
          <Link href={item.enlace} className="font-semibold text-principal hover:text-oro-claro">
            {item.nombre}
          </Link>
          <p className="font-display text-lg font-bold text-principal">{formatearPrecio(item.precioUnitario * item.cantidad)}</p>
        </div>

        {item.detalle.length > 0 && (
          <ul className="mt-1.5 space-y-0.5 text-sm text-suave">
            {item.detalle.map((d) => (
              <li key={d}>{d}</li>
            ))}
          </ul>
        )}

        <div className="mt-3 flex items-center gap-4">
          {/* El número no se escribe: se cambia con − y +, así nunca queda un valor inválido. */}
          <div className="flex items-center rounded-full border border-borde">
            <button
              type="button"
              onClick={() => cambiarCantidad(item.id, item.cantidad - 1)}
              className="grid size-9 place-items-center text-lg text-principal hover:text-oro-claro"
              aria-label={`Quitar uno de ${item.nombre}`}
            >
              −
            </button>
            <span className="min-w-6 text-center text-principal" aria-label={`Cantidad: ${item.cantidad}`}>
              {item.cantidad}
            </span>
            <button
              type="button"
              onClick={() => cambiarCantidad(item.id, item.cantidad + 1)}
              disabled={item.cantidad >= CANTIDAD_MAXIMA}
              className="grid size-9 place-items-center text-lg text-principal hover:text-oro-claro disabled:opacity-40"
              aria-label={`Agregar uno más de ${item.nombre}`}
            >
              +
            </button>
          </div>
          <button
            type="button"
            onClick={() => quitarDelCarrito(item.id)}
            className="text-sm text-suave underline underline-offset-4 hover:text-oro-claro"
          >
            Quitar
          </button>
        </div>
      </div>
    </li>
  );
}
