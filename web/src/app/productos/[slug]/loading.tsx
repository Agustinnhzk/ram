/**
 * Mientras se arma la página de un producto que todavía no estaba hecha (uno cargado después de compilar).
 * También le da a Next.js un "Suspense" alrededor de la página, que es lo que pide para navegar al instante.
 */
export default function Cargando() {
  return (
    <main aria-hidden className="mx-auto max-w-6xl animate-pulse px-4 py-10 sm:px-6 sm:py-14">
      <div className="h-4 w-48 rounded bg-superficie" />
      <div className="mt-8 grid gap-10 lg:grid-cols-2">
        <div className="aspect-square rounded-2xl bg-superficie" />
        <div className="space-y-4">
          <div className="h-12 w-3/4 rounded bg-superficie" />
          <div className="h-6 w-1/2 rounded bg-superficie" />
          <div className="h-32 rounded bg-superficie" />
        </div>
      </div>
    </main>
  );
}
