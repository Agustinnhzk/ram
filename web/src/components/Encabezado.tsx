import Image from "next/image";
import Link from "next/link";
import { obtenerTaller } from "@/lib/api";
import { BotonTema } from "./BotonTema";
import { IconoCarrito, IconoLupa } from "./Iconos";

const secciones = [
  { href: "/productos", texto: "Productos" },
  { href: "/servicios", texto: "Servicios" },
  { href: "/quienes-somos", texto: "Quiénes somos" },
  { href: "/arma-tu-pc", texto: "Armá tu PC" },
];

/**
 * Encabezado de todas las páginas públicas: logo, buscador, cuenta y carrito; debajo, las secciones.
 * Es un componente de servidor: se arma en el servidor y llega al navegador como HTML, sin JavaScript.
 */
export async function Encabezado() {
  const taller = await obtenerTaller();

  return (
    <header className="sticky top-0 z-40 border-b border-borde bg-fondo/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-4 gap-y-3 px-4 py-3 sm:gap-x-6 sm:px-6">
        <Link href="/" className="flex shrink-0 items-center gap-3" aria-label={`${taller.nombreCompleto}, inicio`}>
          <Image src="/marca/logo.png" alt="" width={44} height={44} className="size-11" />
          <span className="leading-tight">
            <span className="block font-display text-xl font-extrabold italic tracking-wide text-principal">
              {taller.nombre}
            </span>
            <span className="block text-xs text-oro">Seguridad y Tecnología</span>
          </span>
        </Link>

        {/* Un formulario común: funciona aun sin JavaScript y lleva a /productos?q=... */}
        <form action="/productos" role="search" className="order-last w-full sm:order-none sm:w-auto sm:flex-1">
          <label htmlFor="buscador" className="sr-only">
            Buscar productos
          </label>
          <div className="flex items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
            <input
              id="buscador"
              name="q"
              type="search"
              placeholder="Buscar cámaras, alarmas, notebooks…"
              className="w-full bg-transparent py-2 text-principal placeholder:text-suave/70 focus:outline-none"
            />
            <button type="submit" className="p-2.5 text-oro hover:text-oro-claro" aria-label="Buscar">
              <IconoLupa className="size-5" />
            </button>
          </div>
        </form>

        <nav aria-label="Cuenta" className="ml-auto flex items-center gap-3 text-sm sm:ml-0 sm:gap-4">
          <BotonTema />
          <Link href="/ingresar" className="text-principal hover:text-oro-claro">
            Ingresá
          </Link>
          {/* En el celular no entra: el registro se ofrece desde "Ingresá". */}
          <Link
            href="/registro"
            className="hidden rounded-full border border-oro px-3.5 py-1.5 text-oro hover:bg-oro hover:text-fondo sm:inline-block"
          >
            Registrate
          </Link>
          <Link href="/carrito" className="relative text-principal hover:text-oro-claro" aria-label="Carrito, 0 productos">
            <IconoCarrito className="size-6" />
            <span className="absolute -top-1.5 -right-2 grid size-4.5 place-items-center rounded-full bg-oro text-[0.65rem] font-semibold text-fondo">
              0
            </span>
          </Link>
        </nav>
      </div>

      <nav aria-label="Secciones" className="border-t border-borde/60">
        <ul className="mx-auto flex max-w-6xl gap-7 overflow-x-auto px-4 py-2.5 text-[0.95rem] whitespace-nowrap sm:px-6">
          {secciones.map((s) => (
            <li key={s.href}>
              <Link href={s.href} className="text-suave hover:text-oro-claro">
                {s.texto}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </header>
  );
}
