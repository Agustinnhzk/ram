import type { ReactNode } from "react";

/** El marco de las pantallas de cuenta (ingresar, registro, recuperar): angosto y centrado. */
export function TarjetaDeCuenta({ titulo, bajada, children }: { titulo: string; bajada?: ReactNode; children: ReactNode }) {
  return (
    <main className="mx-auto max-w-md px-4 py-14 sm:py-20">
      <h1 className="texto-metalico text-5xl">{titulo}</h1>
      {bajada && <p className="mt-3 text-suave">{bajada}</p>}
      <div className="mt-8 rounded-2xl border border-borde bg-superficie p-6 sm:p-7">{children}</div>
    </main>
  );
}
