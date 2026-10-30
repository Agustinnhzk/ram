import type { Metadata } from "next";
import { TrabajaConNosotros } from "@/components/contactos/TrabajaConNosotros";

export const metadata: Metadata = {
  title: "Trabajá con nosotros",
  description: "Sumate al equipo de R.A.M., o tercerizá el soporte IT de tu empresa con nosotros.",
};

export default function Pagina() {
  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Trabajá con nosotros</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Buscamos gente con ganas de aprender y de hacer las cosas bien. Y si tenés una empresa, podemos ser tu área de IT.
      </p>
      <TrabajaConNosotros />
    </main>
  );
}
