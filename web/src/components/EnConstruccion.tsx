import Link from "next/link";

/** Página provisoria para las secciones que todavía no están hechas. */
export function EnConstruccion({ titulo, descripcion }: { titulo: string; descripcion: string }) {
  return (
    <main className="mx-auto max-w-3xl px-4 py-24 sm:px-6">
      <h1 className="texto-metalico text-5xl">{titulo}</h1>
      <p className="mt-5 max-w-prose text-lg text-suave">{descripcion}</p>
      <Link href="/" className="mt-8 inline-block text-oro underline underline-offset-4 hover:text-oro-claro">
        Volver al inicio
      </Link>
    </main>
  );
}
