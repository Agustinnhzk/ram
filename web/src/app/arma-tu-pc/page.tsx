import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Armá tu PC" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Armá tu PC"
      descripcion="Acá vas a poder elegir cada componente de tu PC y ver solo lo que es compatible con lo que ya elegiste."
    />
  );
}
