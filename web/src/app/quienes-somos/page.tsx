import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Quiénes somos" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Quiénes somos"
      descripcion="Acá vas a conocer a R.A.M.: quiénes somos, a qué nos dedicamos y cómo trabajamos."
    />
  );
}
