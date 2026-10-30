"use client";

import Link from "next/link";

/**
 * Si algo falla al armar una página (por ejemplo, el backend no responde), en vez de la pantalla de error
 * de Next se ve esto: un mensaje claro y una salida.
 */
export default function ErrorDePagina({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <main className="mx-auto max-w-3xl px-4 py-24 sm:px-6">
      <h1 className="texto-metalico text-5xl">Algo no anduvo</h1>
      <p className="mt-5 max-w-prose text-lg text-suave">
        No pudimos cargar esta página. Probá de nuevo en un rato, o escribinos por WhatsApp y te ayudamos.
      </p>
      <div className="mt-8 flex flex-wrap gap-3">
        <button type="button" onClick={reset} className="boton-oro rounded-full px-6 py-2.5 font-semibold hover:brightness-110">
          Reintentar
        </button>
        <Link href="/" className="rounded-full border border-borde px-6 py-2.5 text-principal hover:border-oro">
          Ir al inicio
        </Link>
      </div>
    </main>
  );
}
