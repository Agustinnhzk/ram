"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { salir, useSesion } from "@/lib/cuenta";

/**
 * "Ingresá / Registrate", o el nombre del cliente con su menú. De cliente porque la sesión la sabe el navegador:
 * el encabezado es igual para todos (y se guarda en caché), y esta parte se completa al cargar.
 */
export function CuentaEnEncabezado() {
  const sesion = useSesion();

  // Mientras se consulta (un instante), un lugar vacío del mismo ancho: así el encabezado no "salta".
  if (sesion === undefined) return <span aria-hidden className="inline-block h-8 w-16" />;

  if (sesion === null) {
    return (
      <>
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
      </>
    );
  }

  return <MenuDeCuenta nombre={sesion.nombre} />;
}

function MenuDeCuenta({ nombre }: { nombre: string }) {
  const [abierto, setAbierto] = useState(false);
  const contenedor = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const primerNombre = nombre.split(" ")[0];

  // Se cierra al tocar afuera o con Escape.
  useEffect(() => {
    if (!abierto) return;
    const alTocar = (e: PointerEvent) => {
      if (!contenedor.current?.contains(e.target as Node)) setAbierto(false);
    };
    const alTeclear = (e: KeyboardEvent) => e.key === "Escape" && setAbierto(false);
    document.addEventListener("pointerdown", alTocar);
    document.addEventListener("keydown", alTeclear);
    return () => {
      document.removeEventListener("pointerdown", alTocar);
      document.removeEventListener("keydown", alTeclear);
    };
  }, [abierto]);

  return (
    <div ref={contenedor} className="relative">
      <button
        type="button"
        onClick={() => setAbierto(!abierto)}
        aria-expanded={abierto}
        aria-haspopup="true"
        aria-label={`Tu cuenta (${primerNombre})`}
        className="flex items-center gap-2 text-principal hover:text-oro-claro"
      >
        <span className="grid size-8 place-items-center rounded-full border border-oro font-display font-bold text-oro">
          {primerNombre.charAt(0).toUpperCase()}
        </span>
        <span className="hidden max-w-28 truncate sm:inline">{primerNombre}</span>
      </button>
      {abierto && (
        <div className="absolute right-0 z-50 mt-2 w-52 overflow-hidden rounded-xl border border-borde bg-fondo shadow-xl shadow-black/40">
          <p className="truncate border-b border-borde px-4 py-3 text-sm text-suave">Hola, {primerNombre}</p>
          <Link
            href="/mis-pedidos"
            onClick={() => setAbierto(false)}
            className="block px-4 py-2.5 text-principal hover:bg-superficie hover:text-oro-claro"
          >
            Mis pedidos
          </Link>
          <button
            type="button"
            onClick={async () => {
              setAbierto(false);
              await salir();
              router.push("/");
            }}
            className="block w-full px-4 py-2.5 text-left text-principal hover:bg-superficie hover:text-oro-claro"
          >
            Salir
          </button>
        </div>
      )}
    </div>
  );
}
