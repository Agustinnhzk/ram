import type { Metadata } from "next";
import { Checkout } from "@/components/pedidos/Checkout";

export const metadata: Metadata = { title: "Finalizar compra", robots: { index: false } };

export default function Pagina() {
  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Finalizar compra</h1>
      <Checkout />
    </main>
  );
}
