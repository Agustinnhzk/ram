import Image from "next/image";
import Link from "next/link";
import type { Producto } from "@/lib/api";
import { esImagenDeLaApi } from "@/lib/imagenes";
import { linkWhatsApp } from "@/lib/whatsapp";
import { EstadoDeStock } from "./EstadoDeStock";
import { IconoCamara } from "./Iconos";
import { PrecioConOferta } from "./PrecioConOferta";

/**
 * Un producto del catálogo. Toda la tarjeta lleva a la página del producto.
 *
 * Cómo se hace "toda la tarjeta es un link" sin meter un link dentro de otro (eso no es HTML válido):
 * el link es solo el nombre, y con after:absolute after:inset-0 su zona de clic se estira a toda
 * la tarjeta. El botón de WhatsApp va por encima (relative z-10), así que sigue siendo su propio link.
 * Un lector de pantalla lee un solo link con el nombre del producto, no la tarjeta entera.
 */
export function TarjetaProducto({ producto, whatsapp }: { producto: Producto; whatsapp: string }) {
  const reacondicionado = producto.condicion === "reacondicionado";

  return (
    <article className="group relative flex flex-col overflow-hidden rounded-xl border border-borde bg-superficie transition-colors hover:border-oro/60">
      <div className="relative overflow-hidden">
        {producto.miniatura ? (
          <Image
            src={producto.miniatura}
            alt=""
            width={720}
            height={720}
            unoptimized={esImagenDeLaApi(producto.miniatura)}
            sizes="(min-width: 1024px) 25vw, (min-width: 640px) 33vw, 50vw"
            className="aspect-square h-auto w-full bg-white object-cover transition-transform duration-300 group-hover:scale-[1.03]"
          />
        ) : (
          <div className="grid aspect-square place-items-center bg-fondo">
            <IconoCamara className="size-16 text-oro/40" />
          </div>
        )}
        {producto.precioAnterior !== null && (
          <span className="boton-oro absolute top-3 right-3 rounded-full px-2.5 py-0.5 text-xs font-bold">Oferta</span>
        )}
        {reacondicionado && (
          <span className="absolute top-3 left-3 rounded-full bg-negro/80 px-2.5 py-0.5 text-xs text-[#f7d684]">
            Reacondicionada
          </span>
        )}
      </div>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="text-lg text-principal">
          <Link href={`/productos/${producto.slug}`} className="after:absolute after:inset-0 group-hover:text-oro-claro">
            {producto.nombre}
          </Link>
        </h3>
        <p className="mt-1.5 text-sm text-suave">{producto.resumen}</p>
        {producto.garantiaMeses && (
          <p className="mt-2 text-sm text-oro">Garantía de {producto.garantiaMeses} meses</p>
        )}
        {producto.stock && producto.stock.estado !== "DISPONIBLE" && <EstadoDeStock stock={producto.stock} className="mt-2 self-start" />}

        <div className="mt-auto pt-4">
          {producto.precio !== null ? (
            <PrecioConOferta producto={producto} />
          ) : (
            <a
              href={linkWhatsApp(whatsapp, `Hola, quería consultar el precio de: ${producto.nombre}.`)}
              className="relative z-10 inline-block rounded-full border border-oro px-4 py-1.5 text-sm font-semibold text-oro hover:bg-oro hover:text-fondo"
            >
              Consultar precio
            </a>
          )}
        </div>
      </div>
    </article>
  );
}
