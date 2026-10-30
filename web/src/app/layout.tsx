import type { Metadata } from "next";
// Fuentes servidas desde nuestro propio servidor (sin pedirle nada a Google en cada visita).
import "@fontsource-variable/exo-2/wght-italic.css";
import "@fontsource/barlow/400.css";
import "@fontsource/barlow/500.css";
import "@fontsource/barlow/600.css";
import "./globals.css";
import { BotonWhatsApp } from "@/components/BotonWhatsApp";
import { Encabezado } from "@/components/Encabezado";
import { PiePagina } from "@/components/PiePagina";
import { ScriptEnLinea } from "@/components/ScriptEnLinea";
import { URL_DEL_SITIO } from "@/lib/sitio";

export const metadata: Metadata = {
  // Para armar links completos a las imágenes que se ven al compartir
  // (WhatsApp necesita https://.../foto.jpg, no /foto.jpg).
  metadataBase: new URL(URL_DEL_SITIO),
  title: {
    default: "R.A.M. Seguridad y Tecnología | Rosario",
    template: "%s | R.A.M. Seguridad y Tecnología",
  },
  description:
    "Servicio técnico de notebooks y PC, cámaras de seguridad, alarmas y venta de equipos en Rosario. Atención en el local y a domicilio.",
  icons: { icon: "/marca/logo.png" },
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    // data-tema="oscuro": todos entran en oscuro. suppressHydrationWarning: el script de abajo puede
    // cambiar ese atributo antes de que React tome la página, y eso es a propósito.
    <html lang="es-AR" data-tema="oscuro" className="antialiased" suppressHydrationWarning>
      <head>
        {/* Corre antes de pintar: si la persona eligió modo claro otra vez, se aplica sin parpadeo. */}
        <ScriptEnLinea
          codigo={`(function(){try{var t=localStorage.getItem("tema");if(t==="claro"||t==="oscuro")document.documentElement.dataset.tema=t}catch(e){}})()`}
        />
      </head>
      <body className="flex min-h-dvh flex-col">
        <a
          href="#contenido"
          className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-oro focus:px-3 focus:py-2 focus:text-fondo"
        >
          Saltar al contenido
        </a>
        <Encabezado />
        <div id="contenido" className="flex-1">
          {children}
        </div>
        <PiePagina />
        <BotonWhatsApp />
      </body>
    </html>
  );
}
