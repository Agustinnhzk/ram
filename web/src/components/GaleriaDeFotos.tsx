"use client";

import Image from "next/image";
import { useState } from "react";
import { esImagenDeLaApi } from "@/lib/imagenes";
import { IconoCamara } from "./Iconos";

/**
 * La foto grande y, si hay más de una, las miniaturas para cambiarla. De cliente porque elegir una foto es
 * estado de la pantalla (no hace falta que cambie la URL).
 */
export function GaleriaDeFotos({ imagenes, nombre, etiqueta }: { imagenes: string[]; nombre: string; etiqueta?: string }) {
  const [elegida, setElegida] = useState(0);
  const actual = imagenes[elegida];

  return (
    <div className="self-start">
      <div className="relative overflow-hidden rounded-2xl border border-borde bg-white">
        {actual ? (
          // preload: es la imagen más importante de la página, el navegador la pide primero.
          <Image
            key={actual}
            src={actual}
            alt={imagenes.length > 1 ? `${nombre}, foto ${elegida + 1} de ${imagenes.length}` : nombre}
            width={1200}
            height={1200}
            preload={elegida === 0}
            fetchPriority={elegida === 0 ? "high" : undefined}
            unoptimized={esImagenDeLaApi(actual)}
            sizes="(min-width: 768px) 50vw, 100vw"
            className="aspect-square h-auto w-full object-contain"
          />
        ) : (
          <div className="grid aspect-square place-items-center bg-fondo">
            <IconoCamara className="size-24 text-oro/40" />
          </div>
        )}
        {etiqueta && (
          <span className="absolute top-4 left-4 rounded-full bg-negro/80 px-3 py-1 text-sm text-[#f7d684]">{etiqueta}</span>
        )}
      </div>

      {imagenes.length > 1 && (
        <ul className="mt-3 flex gap-2 overflow-x-auto pb-1" aria-label="Fotos del producto">
          {imagenes.map((src, i) => (
            <li key={src} className="shrink-0">
              <button
                type="button"
                onClick={() => setElegida(i)}
                aria-label={`Ver foto ${i + 1}`}
                aria-current={i === elegida ? "true" : undefined}
                className={`block size-16 overflow-hidden rounded-lg border-2 bg-white sm:size-20 ${
                  i === elegida ? "border-oro" : "border-borde hover:border-oro/60"
                }`}
              >
                {/* La miniatura: la versión chica de la misma foto, si viene del backend. */}
                <Image
                  src={esImagenDeLaApi(src) ? `${src}?tam=chica` : src}
                  alt=""
                  width={160}
                  height={160}
                  unoptimized={esImagenDeLaApi(src)}
                  className="size-full object-cover"
                />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
