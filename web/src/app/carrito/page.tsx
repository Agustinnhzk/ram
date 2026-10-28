import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Tu carrito" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Tu carrito"
      descripcion="Acá vas a ver los productos que elegiste para comprar."
    />
  );
}
