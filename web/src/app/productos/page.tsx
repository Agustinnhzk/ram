import type { Metadata } from "next";
import { EnConstruccion } from "@/components/EnConstruccion";

export const metadata: Metadata = { title: "Productos" };

export default function Pagina() {
  return (
    <EnConstruccion
      titulo="Productos"
      descripcion="Acá va a estar el catálogo completo: cámaras, alarmas, notebooks y PC, con filtros por categoría. Mientras tanto, consultanos por WhatsApp."
    />
  );
}
