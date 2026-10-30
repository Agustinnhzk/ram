import type { Metadata } from "next";
import { MisPedidos } from "@/components/pedidos/MisPedidos";

export const metadata: Metadata = { title: "Mis pedidos", robots: { index: false } };

export default function Pagina() {
  return (
    <main className="mx-auto max-w-4xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Mis pedidos</h1>
      <MisPedidos />
    </main>
  );
}
