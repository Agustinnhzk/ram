import type { Metadata } from "next";
import { Suspense } from "react";
import { ConsultaDeSeguimiento } from "@/components/seguimiento/ConsultaDeSeguimiento";
import { obtenerTaller } from "@/lib/api";
import { primero } from "@/lib/catalogo";

export const metadata: Metadata = {
  title: "Seguí tu reparación",
  description: "Consultá en qué está la reparación de tu equipo con el número de orden.",
  // Es una herramienta para clientes, no contenido para buscadores.
  robots: { index: false },
};

/**
 * /seguimiento?orden=900 — el cliente ve en qué está su reparación (ADR-009).
 * El link que le llega por WhatsApp ya trae el número; él solo escribe los últimos 4 de su teléfono.
 */
export default function Pagina({ searchParams }: PageProps<"/seguimiento">) {
  return (
    <main className="mx-auto max-w-3xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Seguí tu reparación</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Ingresá el número de orden y los últimos 4 números de tu teléfono para ver en qué está tu equipo.
      </p>

      <Suspense fallback={<div aria-hidden className="mt-8 h-72 max-w-md animate-pulse rounded-2xl bg-superficie" />}>
        {searchParams.then((parametros) => (
          <Consulta orden={primero(parametros.orden)} />
        ))}
      </Suspense>
    </main>
  );
}

async function Consulta({ orden }: { orden?: string }) {
  const taller = await obtenerTaller();
  // Solo números: lo que venga en la URL no se usa sin revisar.
  const ordenInicial = /^\d{1,7}$/.test(orden ?? "") ? orden! : "";
  return <ConsultaDeSeguimiento ordenInicial={ordenInicial} whatsapp={taller.whatsapp} />;
}
