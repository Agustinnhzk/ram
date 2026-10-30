"use client";

import Image from "next/image";
import Link from "next/link";
import { useRef } from "react";
import type { Promocion } from "@/lib/api";
import { esImagenDeLaApi } from "@/lib/imagenes";

/**
 * Carrusel de flyers. "use client" porque las flechas necesitan JavaScript en el navegador.
 *
 * Usa scroll nativo con scroll-snap: también se puede deslizar con el dedo o el trackpad,
 * y si el JavaScript no carga, sigue funcionando como una fila desplazable.
 */
export function CarruselPromociones({ promociones }: { promociones: Promocion[] }) {
  const fila = useRef<HTMLUListElement>(null);

  function mover(direccion: 1 | -1) {
    const lista = fila.current;
    if (!lista) return;
    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    lista.scrollBy({ left: direccion * lista.clientWidth * 0.85, behavior: reducirMovimiento ? "auto" : "smooth" });
  }

  return (
    <div className="relative">
      <ul
        ref={fila}
        className="flex snap-x snap-mandatory gap-4 overflow-x-auto scroll-smooth pb-4 [scrollbar-width:thin]"
        aria-label="Promociones"
      >
        {promociones.map((p) => (
          <li key={p.id} className="w-[78%] shrink-0 snap-start sm:w-[44%] lg:w-[31%]">
            <Link href={p.enlace} className="block overflow-hidden rounded-xl border border-borde hover:border-oro">
              <Image
                src={p.imagen}
                alt={p.textoAlternativo}
                width={1080}
                height={1080}
                unoptimized={esImagenDeLaApi(p.imagen)}
                sizes="(min-width: 1024px) 31vw, (min-width: 640px) 44vw, 78vw"
                className="aspect-square h-auto w-full object-cover"
              />
            </Link>
          </li>
        ))}
      </ul>

      <div className="mt-2 flex justify-end gap-2">
        <button
          type="button"
          onClick={() => mover(-1)}
          className="grid size-10 place-items-center rounded-full border border-borde text-oro hover:border-oro"
          aria-label="Promociones anteriores"
        >
          <span aria-hidden>‹</span>
        </button>
        <button
          type="button"
          onClick={() => mover(1)}
          className="grid size-10 place-items-center rounded-full border border-borde text-oro hover:border-oro"
          aria-label="Promociones siguientes"
        >
          <span aria-hidden>›</span>
        </button>
      </div>
    </div>
  );
}
