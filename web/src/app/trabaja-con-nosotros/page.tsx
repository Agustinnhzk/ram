import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Trabajá con nosotros" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Trabajá con nosotros"
      descripcion="Acá vas a poder dejarnos tus datos y tu CV."
    />
  );
}
