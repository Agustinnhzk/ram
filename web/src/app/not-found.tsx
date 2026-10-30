import Link from "next/link";

/**
 * La página "no encontrado" (código 404) de toda la web: un link roto, un producto que ya no está,
 * una dirección mal escrita. En vez de un error seco, le damos a la persona por dónde seguir.
 */
export default function NoEncontrado() {
  return (
    <main className="mx-auto max-w-3xl px-4 py-24 text-center sm:px-6">
      <p className="font-display text-7xl font-extrabold italic text-oro">404</p>
      <h1 className="mt-4 text-4xl text-principal">No encontramos esta página</h1>
      <p className="mx-auto mt-4 max-w-md text-lg text-suave">
        Puede que el link esté mal escrito o que el producto ya no esté publicado.
      </p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <Link href="/productos" className="boton-oro rounded-full px-6 py-2.5 font-semibold">
          Ver productos
        </Link>
        <Link href="/" className="rounded-full border border-borde px-6 py-2.5 text-principal hover:border-oro">
          Ir al inicio
        </Link>
      </div>
    </main>
  );
}
