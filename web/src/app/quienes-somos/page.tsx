import type { Metadata } from "next";
import Image from "next/image";
import Link from "next/link";
import { obtenerQuienesSomos, obtenerTaller } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";

export const metadata: Metadata = {
  title: "Quiénes somos",
  description: "R.A.M.: Reparación, Asesoramiento y Mantenimiento. Servicios de IT, seguridad y equipos en Rosario.",
};

/** /quienes-somos: quiénes somos, qué significa el nombre y qué nos define. */
export default async function Pagina() {
  const [contenido, taller] = await Promise.all([obtenerQuienesSomos(), obtenerTaller()]);
  const palabras = contenido.significadoDelNombre?.split(/,\s*|\s+y\s+/) ?? [];

  return (
    <main>
      <section className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-12 sm:px-6 sm:py-16 md:grid-cols-[1.3fr_1fr]">
        <div>
          <h1 className="texto-metalico text-5xl sm:text-6xl">Quiénes somos</h1>
          <p className="mt-6 max-w-xl font-display text-2xl leading-snug font-semibold italic text-principal sm:text-3xl">
            {contenido.presentacion}
          </p>

          {/* La sigla, letra por letra: la inicial de cada palabra en dorado, como en el logo. */}
          {palabras.length > 0 && (
            <div className="mt-8">
              <p className="text-sm text-suave">{taller.nombre} significa</p>
              <ul className="mt-3 space-y-1">
                {palabras.map((palabra) => (
                  <li key={palabra} className="font-display text-2xl font-bold italic text-principal">
                    <span className="texto-metalico pr-0.5 text-4xl font-black">{palabra.charAt(0)}</span>
                    {palabra.slice(1)}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="relative mx-auto w-full max-w-xs">
          <div className="brillo-holograma absolute inset-[10%] rounded-full blur-2xl" aria-hidden />
          <Image
            src="/marca/logo.png"
            alt={`Logo de ${taller.nombreCompleto}`}
            width={512}
            height={512}
            preload
            fetchPriority="high"
            className="relative h-auto w-full"
          />
        </div>
      </section>

      {contenido.datos.length > 0 && (
        <ul className="mx-auto grid max-w-6xl gap-px border-y border-borde bg-borde sm:grid-cols-3">
          {contenido.datos.map((d) => (
            <li key={d.texto} className="bg-fondo px-6 py-6 text-center">
              <p className="font-display text-4xl font-black italic text-oro">{d.valor}</p>
              <p className="mt-1 text-suave">{d.texto}</p>
            </li>
          ))}
        </ul>
      )}

      {contenido.valores.length > 0 && (
        <section aria-labelledby="titulo-valores" className="mx-auto mt-20 max-w-6xl px-4 sm:px-6">
          <h2 id="titulo-valores" className="text-3xl text-principal">
            Cómo trabajamos
          </h2>
          <div className="mt-6 grid gap-8 border-t border-oro/40 pt-8 md:grid-cols-3">
            {contenido.valores.map((v) => (
              <article key={v.titulo}>
                <h3 className="text-2xl text-oro-claro">{v.titulo}</h3>
                <p className="mt-2 text-principal">{v.texto}</p>
              </article>
            ))}
          </div>
        </section>
      )}

      <section className="mx-auto mt-20 max-w-6xl px-4 sm:px-6">
        <div className="flex flex-col items-start justify-between gap-6 rounded-2xl border border-borde bg-superficie px-6 py-8 sm:px-10 md:flex-row md:items-center">
          <div>
            <h2 className="text-3xl text-principal">¿Charlamos?</h2>
            <p className="mt-2 text-suave">Contanos qué necesitás o pasá a ver todo lo que hacemos.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Link href="/servicios" className="boton-oro rounded-full px-6 py-3 font-semibold hover:brightness-110">
              Ver servicios
            </Link>
            <a
              href={linkWhatsApp(taller.whatsapp, "Hola, quería hacer una consulta.")}
              className="rounded-full border border-oro/70 px-6 py-3 font-semibold text-oro-claro hover:border-oro-claro"
            >
              Escribinos por WhatsApp
            </a>
            {taller.correo && (
              <a
                href={`mailto:${taller.correo}`}
                className="rounded-full border border-borde px-6 py-3 font-semibold text-principal hover:border-oro-claro"
              >
                {taller.correo}
              </a>
            )}
          </div>
        </div>
        <p className="mt-6 text-suave">
          ¿Querés sumarte al equipo?{" "}
          <Link href="/trabaja-con-nosotros" className="text-oro underline underline-offset-4 hover:text-oro-claro">
            Trabajá con nosotros
          </Link>
        </p>
      </section>
    </main>
  );
}
