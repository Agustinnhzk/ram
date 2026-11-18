"use client";

import { useState } from "react";
import type { VideoIncrustable } from "@/lib/video";

/**
 * El video de un trabajo (reel de Instagram o YouTube). Hasta que se toca "play" es solo un botón:
 * el reproductor de Instagram o YouTube pesa bastante y haría más lenta la página aunque nadie lo mire.
 */
export function VideoDelTrabajo({ video, titulo }: { video: VideoIncrustable; titulo: string }) {
  const [reproduciendo, setReproduciendo] = useState(false);
  const proporcion = video.vertical ? "aspect-[9/16] max-w-xs" : "aspect-video";

  return (
    <figure className="mx-auto w-full">
      <div className={`relative mx-auto w-full overflow-hidden rounded-xl border border-borde bg-superficie ${proporcion}`}>
        {reproduciendo ? (
          <iframe
            src={video.reproductor}
            title={`Video: ${titulo}`}
            className="absolute inset-0 size-full"
            allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
            allowFullScreen
            loading="lazy"
            referrerPolicy="strict-origin-when-cross-origin"
          />
        ) : (
          <button
            type="button"
            onClick={() => setReproduciendo(true)}
            className="group absolute inset-0 grid place-items-center text-center"
            aria-label={`Ver el video en ${video.plataforma}`}
          >
            <span className="grid justify-items-center gap-3">
              <span className="grid size-16 place-items-center rounded-full bg-oro/90 text-negro transition-transform group-hover:scale-110">
                <svg viewBox="0 0 24 24" className="ml-1 size-7" fill="currentColor" aria-hidden>
                  <path d="M8 5.5v13a1 1 0 0 0 1.5.9l10.5-6.5a1 1 0 0 0 0-1.7L9.5 4.6A1 1 0 0 0 8 5.5Z" />
                </svg>
              </span>
              <span className="text-sm font-semibold text-principal">Ver el video</span>
              <span className="text-xs text-suave">Se reproduce desde {video.plataforma}</span>
            </span>
          </button>
        )}
      </div>
      <figcaption className="mt-2 text-center text-xs text-suave">
        <a href={video.original} target="_blank" rel="noopener noreferrer" className="underline underline-offset-4 hover:text-oro-claro">
          Abrir en {video.plataforma} ↗
        </a>
      </figcaption>
    </figure>
  );
}
