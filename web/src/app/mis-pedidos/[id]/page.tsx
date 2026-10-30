import type { Metadata } from "next";
import { Suspense } from "react";
import { DetalleDeMiPedido } from "@/components/pedidos/DetalleDeMiPedido";
import { obtenerTaller } from "@/lib/api";

export const metadata: Metadata = { title: "Tu pedido", robots: { index: false } };

/**
 * /mis-pedidos/{id}: el pedido, con el alias para transferir y la carga del comprobante.
 * Los datos los trae el navegador (son de la cuenta del cliente): el servidor solo arma el marco.
 */
export default function Pagina({ params }: PageProps<"/mis-pedidos/[id]">) {
  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Tu pedido</h1>
      <Suspense fallback={<div aria-hidden className="mt-10 h-72 animate-pulse rounded-xl bg-superficie" />}>
        {Promise.all([params, obtenerTaller()]).then(([{ id }, taller]) => (
          <DetalleDeMiPedido id={id} whatsapp={taller.whatsapp} />
        ))}
      </Suspense>
    </main>
  );
}
