import { cacheLife } from "next/cache";
import Image from "next/image";
import Link from "next/link";
import { obtenerTaller } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";
import { IconoInstagram, IconoWhatsApp } from "./Iconos";

export async function PiePagina() {
  // Componente cacheado: así puede usar la fecha actual (el año del copyright) sin que Next.js
  // tenga que armarlo de nuevo en cada visita. Se renueva una vez por día.
  "use cache";
  cacheLife("days");
  const taller = await obtenerTaller();

  return (
    <footer className="mt-24 border-t border-borde bg-superficie">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.4fr_1fr_1fr_auto]">
        <div>
          <div className="flex items-center gap-3">
            <Image src="/marca/logo.png" alt="" width={48} height={48} />
            <p className="font-display text-xl font-extrabold italic text-principal">{taller.nombreCompleto}</p>
          </div>
          <p className="mt-4 font-display text-lg italic text-oro">{taller.eslogan}</p>
          <p className="mt-1 text-sm text-suave">{taller.ciudad}, Santa Fe</p>
        </div>

        <div>
          <h2 className="font-sans text-base font-semibold not-italic text-principal">Seguinos en</h2>
          <ul className="mt-3 space-y-2.5 text-suave">
            <li>
              <a href={`https://instagram.com/${taller.instagram}`} className="inline-flex items-center gap-2 hover:text-oro-claro">
                <IconoInstagram className="size-5 text-oro" />@{taller.instagram}
              </a>
            </li>
            <li>
              <a
                href={linkWhatsApp(taller.whatsapp, "Hola, quería hacer una consulta.")}
                className="inline-flex items-center gap-2 hover:text-oro-claro"
              >
                <IconoWhatsApp className="size-5 text-oro" />
                {taller.whatsappVisible}
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h2 className="font-sans text-base font-semibold not-italic text-principal">{taller.nombre}</h2>
          <ul className="mt-3 space-y-2.5 text-suave">
            <li>
              <Link href="/quienes-somos" className="hover:text-oro-claro">
                Sobre nosotros
              </Link>
            </li>
            <li>
              <Link href="/trabaja-con-nosotros" className="hover:text-oro-claro">
                Trabajá con nosotros
              </Link>
            </li>
          </ul>
        </div>

        {/* QR de Data Fiscal (ARCA). Por ahora un lugar reservado: la imagen real se va a cargar desde el sistema. */}
        <div className="flex flex-col items-start md:items-center">
          <div className="grid size-24 place-items-center rounded-md border border-dashed border-oro/60 text-center text-xs text-suave">
            QR Data Fiscal
          </div>
        </div>
      </div>

      <p className="border-t border-borde/60 py-5 text-center text-sm text-suave/80">
        © {new Date().getFullYear()} {taller.nombreCompleto}
      </p>
    </footer>
  );
}
