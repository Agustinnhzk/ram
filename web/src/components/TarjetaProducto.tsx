import Image from "next/image";
import type { Producto } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";

const formatoPrecio = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 });

/**
 * Un producto del catálogo. Sin precio cargado, el botón lleva a WhatsApp con el producto
 * ya nombrado en el mensaje: es como funciona la venta hasta que se sume el pago online.
 */
export function TarjetaProducto({ producto, whatsapp }: { producto: Producto; whatsapp: string }) {
  const reacondicionado = producto.condicion === "reacondicionado";

  return (
    <article className="flex flex-col overflow-hidden rounded-xl border border-borde bg-superficie">
      <div className="relative">
        <Image
          src={producto.imagen}
          alt={producto.nombre}
          width={720}
          height={720}
          sizes="(min-width: 1024px) 25vw, (min-width: 640px) 33vw, 50vw"
          className="aspect-square h-auto w-full object-cover"
        />
        {reacondicionado && (
          <span className="absolute top-3 left-3 rounded-full bg-negro/80 px-2.5 py-0.5 text-xs text-[#f7d684]">
            Reacondicionada
          </span>
        )}
      </div>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="text-lg text-principal">{producto.nombre}</h3>
        <p className="mt-1.5 text-sm text-suave">{producto.resumen}</p>
        {producto.garantiaMeses && (
          <p className="mt-2 text-sm text-oro">Garantía de {producto.garantiaMeses} meses</p>
        )}

        <div className="mt-auto pt-4">
          {producto.precio !== null ? (
            <p className="font-display text-xl font-bold text-principal">{formatoPrecio.format(producto.precio)}</p>
          ) : (
            <a
              href={linkWhatsApp(whatsapp, `Hola, quería consultar el precio de: ${producto.nombre}.`)}
              className="inline-block rounded-full border border-oro px-4 py-1.5 text-sm font-semibold text-oro hover:bg-oro hover:text-fondo"
            >
              Consultar precio
            </a>
          )}
        </div>
      </div>
    </article>
  );
}
