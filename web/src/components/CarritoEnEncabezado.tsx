"use client";

import Link from "next/link";
import { cantidadDeArticulos, useCarrito } from "@/lib/carrito";
import { IconoCarrito } from "./Iconos";

/**
 * El ícono del carrito con la cantidad. Es un componente de cliente porque el carrito vive en el
 * navegador: el servidor no sabe qué hay. Mientras el navegador lo lee, se ve el ícono sin número.
 */
export function CarritoEnEncabezado() {
  const items = useCarrito();
  const cantidad = items ? cantidadDeArticulos(items) : 0;
  const etiqueta = cantidad === 1 ? "Carrito, 1 producto" : `Carrito, ${cantidad} productos`;

  return (
    <Link href="/carrito" className="relative text-principal hover:text-oro-claro" aria-label={etiqueta}>
      <IconoCarrito className="size-6" />
      {cantidad > 0 && (
        <span className="absolute -top-1.5 -right-2 grid size-4.5 place-items-center rounded-full bg-oro text-[0.65rem] font-semibold text-fondo">
          {cantidad > 9 ? "9+" : cantidad}
        </span>
      )}
    </Link>
  );
}
