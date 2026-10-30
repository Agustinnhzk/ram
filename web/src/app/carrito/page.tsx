import type { Metadata } from "next";
import { Carrito } from "@/components/Carrito";
import { obtenerTaller } from "@/lib/api";

export const metadata: Metadata = { title: "Carrito", robots: { index: false } };

/** /carrito: la página la arma el servidor; la lista la completa el navegador (ver components/Carrito). */
export default async function Pagina() {
  const taller = await obtenerTaller();

  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Tu carrito</h1>
      <Carrito whatsapp={taller.whatsapp} />
    </main>
  );
}
