import Image from "next/image";
import Link from "next/link";
import type { Trabajo } from "@/lib/api";
import { esImagenDeLaApi } from "@/lib/imagenes";
import { mesDelTrabajo, NOMBRE_DEL_TIPO } from "@/lib/trabajos";
import { IconoAlarma, IconoCamara, IconoDomicilio, IconoHerramienta, IconoNotebook, IconoPc } from "./Iconos";

const ICONO = { notebook: IconoNotebook, pc: IconoPc, camaras: IconoCamara, alarmas: IconoAlarma, otro: IconoHerramienta };

/**
 * Un trabajo realizado en el listado. Toda la tarjeta lleva al caso (mismo truco que TarjetaProducto:
 * el link es el título y su zona de clic se estira con after:absolute).
 */
export function TarjetaTrabajo({ trabajo }: { trabajo: Trabajo }) {
  const Icono = ICONO[trabajo.tipo];

  return (
    <article className="group relative flex flex-col overflow-hidden rounded-xl border border-borde bg-superficie transition-colors hover:border-oro/60">
      <div className="relative overflow-hidden">
        {trabajo.miniatura ? (
          <Image
            src={trabajo.miniatura}
            alt=""
            width={600}
            height={450}
            unoptimized={esImagenDeLaApi(trabajo.miniatura)}
            sizes="(min-width: 1024px) 33vw, (min-width: 640px) 50vw, 100vw"
            className="aspect-[4/3] h-auto w-full bg-fondo object-cover transition-transform duration-300 group-hover:scale-[1.03]"
          />
        ) : (
          <div className="grid aspect-[4/3] place-items-center bg-fondo">
            <Icono className="size-16 text-oro/40" />
          </div>
        )}
        <span className="absolute top-3 left-3 rounded-full bg-negro/80 px-2.5 py-0.5 text-xs text-[#f7d684]">{trabajo.barrio}</span>
      </div>

      <div className="flex flex-1 flex-col p-4">
        <p className="text-xs tracking-wide text-oro uppercase">{NOMBRE_DEL_TIPO[trabajo.tipo]}</p>
        <h3 className="mt-1 text-xl text-principal">
          <Link href={`/trabajos/${trabajo.slug}`} className="after:absolute after:inset-0 group-hover:text-oro-claro">
            {trabajo.titulo}
          </Link>
        </h3>
        <p className="mt-2 line-clamp-3 text-sm text-suave">{trabajo.problema}</p>
        <p className="mt-auto flex flex-wrap items-center gap-x-3 pt-4 text-xs text-suave">
          {trabajo.aDomicilio && (
            <span className="inline-flex items-center gap-1">
              <IconoDomicilio className="size-4 text-oro" />A domicilio
            </span>
          )}
          <span>{mesDelTrabajo(trabajo.fecha)}</span>
        </p>
      </div>
    </article>
  );
}
