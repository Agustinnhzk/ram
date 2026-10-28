import { obtenerTaller } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";
import { IconoWhatsApp } from "./Iconos";

/** Botón flotante en todas las páginas para pedir turno o consultar (decisión del documento de arquitectura). */
export async function BotonWhatsApp() {
  const taller = await obtenerTaller();

  return (
    <a
      href={linkWhatsApp(taller.whatsapp, "Hola, quiero pedir un turno.")}
      className="fixed right-4 bottom-4 z-50 flex items-center gap-2 rounded-full bg-whatsapp py-3 pr-5 pl-3.5 font-semibold text-negro shadow-[0_6px_24px_rgb(37_211_102/0.35)] hover:brightness-110 sm:right-6 sm:bottom-6"
    >
      <IconoWhatsApp className="size-6" />
      <span>Pedir turno</span>
    </a>
  );
}
