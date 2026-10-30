"use client";

import Link from "next/link";
import { useState } from "react";
import { agregarAlCarrito, type ItemCarrito } from "@/lib/carrito";
import { IconoCarrito, IconoCheck } from "./Iconos";

/**
 * "Agregar al carrito". Después de agregar, el botón confirma y ofrece ir al carrito,
 * sin llevarte a otra página: la persona decide si sigue mirando o va a pagar.
 * aria-live: quien usa lector de pantalla escucha "Agregado al carrito".
 */
export function BotonAgregarAlCarrito({
  item,
  className = "",
  texto = "Agregar al carrito",
}: {
  item: Omit<ItemCarrito, "cantidad">;
  className?: string;
  /** "Reservar" cuando no hay stock (ADR-014): se compra igual. */
  texto?: string;
}) {
  const [agregado, setAgregado] = useState(false);

  return (
    <div className={`flex flex-wrap items-center gap-x-4 gap-y-2 ${className}`}>
      <button
        type="button"
        onClick={() => {
          agregarAlCarrito(item);
          setAgregado(true);
        }}
        className="boton-oro inline-flex items-center gap-2 rounded-full px-6 py-3 font-semibold hover:brightness-110"
      >
        <IconoCarrito className="size-5" />
        {texto}
      </button>
      <p aria-live="polite" className="text-sm">
        {agregado && (
          <span className="inline-flex items-center gap-2 text-principal">
            <IconoCheck className="size-4 text-oro" />
            Agregado al carrito ·{" "}
            <Link href="/carrito" className="text-oro underline underline-offset-4 hover:text-oro-claro">
              Ver carrito
            </Link>
          </span>
        )}
      </p>
    </div>
  );
}
