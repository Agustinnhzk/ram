import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Servicios" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Servicios"
      descripcion="Acá vas a encontrar el detalle de reparaciones, instalaciones y monitoreo, con fotos de trabajos y opiniones de clientes."
    />
  );
}
