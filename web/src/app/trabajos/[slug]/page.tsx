import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { DatosEstructurados } from "@/components/DatosEstructurados";
import { GaleriaDeFotos } from "@/components/GaleriaDeFotos";
import { IconoDomicilio, IconoWhatsApp } from "@/components/Iconos";
import { TarjetaTrabajo } from "@/components/TarjetaTrabajo";
import { obtenerOtrosTrabajos, obtenerSlugsDeTrabajos, obtenerTaller, obtenerTrabajo, type Trabajo } from "@/lib/api";
import { urlCompleta } from "@/lib/sitio";
import { mesDelTrabajo, NOMBRE_DEL_TIPO, SERVICIO_DEL_TIPO } from "@/lib/trabajos";
import { linkWhatsApp } from "@/lib/whatsapp";

/** Como los productos: cada trabajo publicado se arma de antemano, y uno nuevo en su primera visita. */
export async function generateStaticParams() {
  const slugs = await obtenerSlugsDeTrabajos();
  return slugs.map((slug) => ({ slug }));
}

/** "Notebook que no encendía en Abasto | R.A.M. ...": el barrio en el título es lo que ayuda en Google. */
export async function generateMetadata({ params }: PageProps<"/trabajos/[slug]">): Promise<Metadata> {
  const { slug } = await params;
  const trabajo = await obtenerTrabajo(slug);
  if (!trabajo) return { title: "Trabajo no encontrado" };

  const titulo = `${trabajo.titulo} en ${trabajo.barrio}`;
  return {
    title: titulo,
    description: recortar(`${trabajo.problema} ${trabajo.solucion}`, 155),
    alternates: { canonical: `/trabajos/${trabajo.slug}` },
    openGraph: {
      title: titulo,
      description: recortar(trabajo.problema, 155),
      url: `/trabajos/${trabajo.slug}`,
      siteName: "R.A.M. Seguridad y Tecnología",
      locale: "es_AR",
      type: "article",
      images: trabajo.imagen ? [trabajo.imagen] : ["/marca/logo.png"],
    },
  };
}

/** /trabajos/[slug] — un trabajo realizado: el problema, qué se hizo, el barrio y las fotos. */
export default async function Pagina({ params }: PageProps<"/trabajos/[slug]">) {
  const { slug } = await params;
  const trabajo = await obtenerTrabajo(slug);
  if (!trabajo) notFound();

  const [taller, otros] = await Promise.all([obtenerTaller(), obtenerOtrosTrabajos(trabajo)]);
  const servicio = SERVICIO_DEL_TIPO[trabajo.tipo];

  return (
    <main className="mx-auto max-w-6xl px-4 py-10 sm:px-6 sm:py-14">
      <DatosEstructurados datos={datosDelTrabajo(trabajo, taller.nombreCompleto)} />
      <nav aria-label="Estás en" className="text-sm text-suave">
        <ol className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <li>
            <Link href="/trabajos" className="hover:text-oro-claro">
              Trabajos realizados
            </Link>
          </li>
          <li aria-hidden>/</li>
          <li aria-current="page" className="text-principal">
            {trabajo.titulo}
          </li>
        </ol>
      </nav>

      <div className="mt-6 grid gap-8 md:grid-cols-2 md:gap-12">
        {trabajo.imagenes.length > 0 && <GaleriaDeFotos imagenes={trabajo.imagenes} nombre={trabajo.titulo} />}

        <div className={trabajo.imagenes.length > 0 ? "" : "md:col-span-2 md:max-w-3xl"}>
          <p className="text-sm tracking-wide text-oro uppercase">{NOMBRE_DEL_TIPO[trabajo.tipo]}</p>
          <h1 className="mt-1 text-4xl text-principal sm:text-5xl">{trabajo.titulo}</h1>
          <ul className="mt-4 flex flex-wrap gap-2 text-sm">
            <li className="rounded-full border border-oro/60 px-3 py-1 text-oro-claro">{trabajo.barrio}</li>
            {trabajo.aDomicilio && (
              <li className="inline-flex items-center gap-1.5 rounded-full border border-borde px-3 py-1 text-suave">
                <IconoDomicilio className="size-4 text-oro" />A domicilio
              </li>
            )}
            <li className="rounded-full border border-borde px-3 py-1 text-suave">{mesDelTrabajo(trabajo.fecha)}</li>
          </ul>

          <dl className="mt-8 space-y-6">
            <Parte titulo="El problema" texto={trabajo.problema} />
            {trabajo.diagnostico && <Parte titulo="El diagnóstico" texto={trabajo.diagnostico} />}
            <Parte titulo="Qué hicimos" texto={trabajo.solucion} />
          </dl>

          <div className="mt-8 border-t border-borde pt-6">
            <a
              href={linkWhatsApp(taller.whatsapp, `Hola, vi el trabajo "${trabajo.titulo}" en la web y tengo algo parecido.`)}
              className="inline-flex items-center gap-2 rounded-full bg-whatsapp px-6 py-3 font-semibold text-negro hover:brightness-110"
            >
              <IconoWhatsApp className="size-5" />
              Tengo algo parecido
            </a>
            <Link href={servicio.href} className="mt-4 block text-sm font-semibold text-oro underline underline-offset-4 hover:text-oro-claro">
              {servicio.texto}
            </Link>
          </div>
        </div>
      </div>

      {otros.length > 0 && (
        <section aria-labelledby="otros" className="mt-16">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <h2 id="otros" className="text-3xl text-principal">
              Otros trabajos
            </h2>
            <Link href="/trabajos" className="font-semibold text-oro underline decoration-oro/40 underline-offset-4 hover:text-oro-claro">
              Ver todos
            </Link>
          </div>
          <div className="mt-6 grid gap-4 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
            {otros.map((t) => (
              <TarjetaTrabajo key={t.slug} trabajo={t} />
            ))}
          </div>
        </section>
      )}
    </main>
  );
}

function Parte({ titulo, texto }: { titulo: string; texto: string }) {
  return (
    <div>
      <dt className="font-display text-xl font-bold text-principal italic">{titulo}</dt>
      <dd className="mt-1 whitespace-pre-line text-suave">{texto}</dd>
    </div>
  );
}

function recortar(texto: string, largo: number): string {
  const limpio = texto.replace(/\s+/g, " ").trim();
  return limpio.length <= largo ? limpio : `${limpio.slice(0, largo - 1).replace(/\s+\S*$/, "")}…`;
}

/**
 * Para Google: es un artículo de este negocio (schema.org Article), con la fecha y las fotos.
 * Sin datos del cliente: solo el barrio, igual que en la página.
 */
function datosDelTrabajo(trabajo: Trabajo, negocio: string) {
  return {
    "@context": "https://schema.org",
    "@type": "Article",
    headline: `${trabajo.titulo} en ${trabajo.barrio}`,
    description: recortar(trabajo.problema, 200),
    datePublished: trabajo.fecha,
    url: urlCompleta(`/trabajos/${trabajo.slug}`),
    image: trabajo.imagenes.map((imagen) => urlCompleta(imagen)),
    author: { "@type": "Organization", name: negocio, url: urlCompleta("/") },
    publisher: { "@type": "Organization", name: negocio, logo: urlCompleta("/marca/logo.png") },
    contentLocation: { "@type": "Place", name: trabajo.barrio },
  };
}
