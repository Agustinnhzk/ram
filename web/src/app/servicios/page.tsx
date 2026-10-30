import type { Metadata } from "next";
import Link from "next/link";
import type { ComponentType } from "react";
import {
  IconoAlarma,
  IconoCamara,
  IconoCarrito,
  IconoCheck,
  IconoCodigo,
  IconoHerramienta,
  IconoMonitoreo,
  IconoNotebook,
  IconoPc,
  IconoWhatsApp,
} from "@/components/Iconos";
import { obtenerServicios, obtenerTaller, type IconoDeServicio, type Servicio } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";

export const metadata: Metadata = {
  title: "Servicios",
  description:
    "Instalación de cámaras y alarmas, monitoreo, reparación de notebooks y PC, armado de PC y venta de equipos en Rosario, a domicilio o en el local.",
};

/** El nombre del ícono viene en los datos; el dibujo lo pone la web. */
const iconos: Record<IconoDeServicio, ComponentType<{ className?: string }>> = {
  camara: IconoCamara,
  alarma: IconoAlarma,
  monitoreo: IconoMonitoreo,
  notebook: IconoNotebook,
  pc: IconoPc,
  armado: IconoHerramienta,
  venta: IconoCarrito,
  codigo: IconoCodigo,
};

/**
 * /servicios: todos los servicios, agrupados. No depende de la URL, así que Next.js
 * la arma una sola vez y la manda ya hecha (sin <Suspense>, a diferencia del catálogo).
 *
 * Cada grupo tiene un id (#instalaciones, #reparacion, #venta) para que otros links lleven directo ahí.
 */
export default async function Pagina() {
  const [grupos, taller] = await Promise.all([obtenerServicios(), obtenerTaller()]);
  const destacados = grupos.filter((g) => g.destacado);
  const secundarios = grupos.filter((g) => !g.destacado);

  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Servicios</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Seguridad, servicio técnico y equipos en un solo lugar. Te atendemos en el local o a domicilio en {taller.ciudad}.
      </p>

      {/* Índice: saltos a cada grupo dentro de la misma página. */}
      <nav aria-label="Grupos de servicios" className="mt-8">
        <ul className="flex flex-wrap gap-2">
          {destacados.map((g) => (
            <li key={g.slug}>
              <a
                href={`#${g.slug}`}
                className="inline-block rounded-full border border-borde px-4 py-1.5 text-sm text-principal hover:border-oro hover:text-oro-claro"
              >
                {g.titulo}
              </a>
            </li>
          ))}
        </ul>
      </nav>

      {destacados.map((grupo) => (
        // scroll-mt: al saltar con #ancla, el título no queda tapado por el encabezado fijo.
        <section key={grupo.slug} id={grupo.slug} aria-labelledby={`titulo-${grupo.slug}`} className="mt-16 scroll-mt-44">
          <div className="border-t border-oro/40 pt-8">
            <h2 id={`titulo-${grupo.slug}`} className="text-3xl text-oro-claro sm:text-4xl">
              {grupo.titulo}
            </h2>
            <p className="mt-3 max-w-prose text-principal">{grupo.descripcion}</p>
            {grupo.ventajas.length > 0 && (
              <ul className="mt-4 flex flex-wrap gap-2">
                {grupo.ventajas.map((v) => (
                  <li key={v} className="rounded-full border border-borde px-3 py-1 text-sm text-suave">
                    {v}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="mt-8 grid gap-4 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
            {grupo.servicios.map((s) => (
              <TarjetaServicio key={s.slug} servicio={s} whatsapp={taller.whatsapp} />
            ))}
          </div>
        </section>
      ))}

      {/* Los grupos no destacados (desarrollo de software) van al final, en una línea, sin tarjetas. */}
      {secundarios.map((grupo) => (
        <section
          key={grupo.slug}
          id={grupo.slug}
          aria-labelledby={`titulo-${grupo.slug}`}
          className="mt-16 flex scroll-mt-44 flex-wrap items-center gap-x-4 gap-y-2 text-suave"
        >
          <IconoCodigo className="size-5 shrink-0 text-oro" />
          <h2 id={`titulo-${grupo.slug}`} className="font-sans text-base font-semibold not-italic text-principal">
            {grupo.titulo}
          </h2>
          <p>
            {grupo.descripcion}{" "}
            <a
              href={linkWhatsApp(taller.whatsapp, `Hola, quería consultar por ${grupo.titulo.toLowerCase()}.`)}
              className="text-oro underline underline-offset-4 hover:text-oro-claro"
            >
              Escribinos
            </a>
          </p>
        </section>
      ))}

      {/* Cierre: el nombre lo dice (Asesoramiento), así que lo ofrecemos. */}
      <section className="mt-16 rounded-2xl border border-borde bg-superficie px-6 py-10 text-center sm:px-10">
        <h2 className="text-3xl text-principal">¿No sabés qué necesitás?</h2>
        <p className="mx-auto mt-3 max-w-md text-suave">Contanos qué te pasa o qué querés proteger y te asesoramos sin compromiso.</p>
        <a
          href={linkWhatsApp(taller.whatsapp, "Hola, quería que me asesoren.")}
          className="mt-6 inline-flex items-center gap-2 rounded-full bg-whatsapp px-6 py-3 font-semibold text-negro hover:brightness-110"
        >
          <IconoWhatsApp className="size-5" />
          Pedir asesoramiento
        </a>
        {taller.correo && (
          <p className="mt-4 text-sm text-suave">
            O escribinos a{" "}
            <a href={`mailto:${taller.correo}`} className="text-oro underline underline-offset-4 hover:text-oro-claro">
              {taller.correo}
            </a>
          </p>
        )}
      </section>
    </main>
  );
}

function TarjetaServicio({ servicio, whatsapp }: { servicio: Servicio; whatsapp: string }) {
  const Icono = iconos[servicio.icono];

  return (
    <article className="flex flex-col rounded-xl border border-borde bg-superficie p-6">
      <Icono className="size-9 text-oro" />
      <h3 className="mt-4 text-xl text-principal">{servicio.nombre}</h3>
      <p className="mt-2 text-suave">{servicio.resumen}</p>

      <ul className="mt-4 space-y-2">
        {servicio.incluye.map((i) => (
          <li key={i} className="flex items-start gap-2.5 text-sm text-principal">
            <IconoCheck className="mt-0.5 size-4 shrink-0 text-oro" />
            {i}
          </li>
        ))}
      </ul>

      <div className="mt-auto flex flex-wrap items-center gap-x-5 gap-y-3 pt-6">
        <a
          href={linkWhatsApp(whatsapp, `Hola, quería consultar por: ${servicio.nombre}.`)}
          className="inline-flex items-center gap-2 rounded-full border border-oro px-4 py-1.5 text-sm font-semibold text-oro hover:bg-oro hover:text-fondo"
        >
          <IconoWhatsApp className="size-4" />
          Consultar
        </a>
        {servicio.enlace && (
          <Link href={servicio.enlace.href} className="text-sm text-oro underline underline-offset-4 hover:text-oro-claro">
            {servicio.enlace.texto}
          </Link>
        )}
      </div>
    </article>
  );
}
