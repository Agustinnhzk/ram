import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Ingresá" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Ingresá"
      descripcion="Acá vas a poder iniciar sesión para comprar y ver tus pedidos y reparaciones."
    />
  );
}
