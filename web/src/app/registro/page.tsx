import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Registrate" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Registrate"
      descripcion="Acá vas a poder crear tu cuenta para comprar y seguir tus reparaciones."
    />
  );
}
