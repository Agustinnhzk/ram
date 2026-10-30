import Form from "next/form";
import Image from "next/image";
import Link from "next/link";
import { obtenerTaller } from "@/lib/api";
import { BotonTema } from "./BotonTema";
import { CarritoEnEncabezado } from "./CarritoEnEncabezado";
import { CuentaEnEncabezado } from "./CuentaEnEncabezado";
import { IconoLupa } from "./Iconos";

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

        {/* <Form> de Next: funciona aun sin JavaScript y, con JavaScript, lleva a /productos?q=... sin recargar todo. */}
        <Form action="/productos" role="search" className="order-last w-full sm:order-none sm:w-auto sm:flex-1">
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
        </Form>

        <nav aria-label="Cuenta" className="ml-auto flex items-center gap-3 text-sm sm:ml-0 sm:gap-4">
          <BotonTema />
          <CuentaEnEncabezado />
          <CarritoEnEncabezado />
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
