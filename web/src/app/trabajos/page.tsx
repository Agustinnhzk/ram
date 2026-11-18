import type { Metadata } from "next";
import { IconoWhatsApp } from "@/components/Iconos";
import { TarjetaTrabajo } from "@/components/TarjetaTrabajo";
import { obtenerTaller, obtenerTrabajos, type Trabajo } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";

/** Mientras no haya ningún trabajo publicado, la página existe pero no se indexa (una página vacía no le suma a Google). */
export async function generateMetadata(): Promise<Metadata> {
  "use cache";
  const trabajos = await obtenerTrabajos();
  return {
    title: "Trabajos realizados en Rosario y alrededores",
    description:
      "Reparaciones de notebooks y PC, e instalaciones de cámaras y alarmas que hicimos en Rosario y alrededores, con el barrio y fotos de cada trabajo.",
    alternates: { canonical: "/trabajos" },
    ...(trabajos.length === 0 && { robots: { index: false } }),
  };
}

/** /trabajos — casos reales con el barrio donde se hicieron (ADR-018). Se cargan desde el sistema. */
export default async function Pagina() {
  const [trabajos, taller] = await Promise.all([obtenerTrabajos(), obtenerTaller()]);
  const zonas = zonasCon(trabajos);

  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Trabajos realizados</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Algunos de los trabajos que hicimos en {taller.ciudad} y alrededores: qué le pasaba a cada equipo o qué necesitaba cada
        casa, y cómo lo resolvimos.
      </p>

      {zonas.length > 0 && (
        <section aria-labelledby="zonas" className="mt-8">
          <h2 id="zonas" className="font-sans text-base font-semibold not-italic text-principal">
            Barrios y localidades donde trabajamos
          </h2>
          <ul className="mt-3 flex flex-wrap gap-2">
            {zonas.map((z) => (
              <li key={z.clave} className="rounded-full border border-borde px-3 py-1 text-sm text-suave">
                {z.nombre}
                {z.cantidad > 1 && <span className="text-oro"> · {z.cantidad}</span>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {trabajos.length > 0 ? (
        <div className="mt-10 grid gap-4 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
          {trabajos.map((t) => (
            <TarjetaTrabajo key={t.slug} trabajo={t} />
          ))}
        </div>
      ) : (
        <p className="mt-10 rounded-xl border border-dashed border-borde px-6 py-12 text-center text-suave">
          Muy pronto vas a ver acá los trabajos que hicimos.
        </p>
      )}

      <section className="mt-16 rounded-2xl border border-oro/50 bg-superficie p-8 text-center">
        <h2 className="text-3xl text-principal">¿Tenés un problema parecido?</h2>
        <p className="mx-auto mt-3 max-w-prose text-suave">Contanos qué le pasa a tu equipo o qué necesitás instalar y te respondemos por WhatsApp.</p>
        <a
          href={linkWhatsApp(taller.whatsapp, "Hola, vi los trabajos en la web y quería hacer una consulta.")}
          className="mt-6 inline-flex items-center gap-2 rounded-full bg-whatsapp px-6 py-3 font-semibold text-negro hover:brightness-110"
        >
          <IconoWhatsApp className="size-5" />
          Escribinos por WhatsApp
        </a>
      </section>
    </main>
  );
}

/** Un chip por zona ("Alberdi · 3"), de la que tiene más trabajos a la que menos. */
function zonasCon(trabajos: Trabajo[]) {
  const porClave = new Map<string, { clave: string; nombre: string; cantidad: number }>();
  for (const t of trabajos) {
    const zona = porClave.get(t.barrioClave);
    if (zona) zona.cantidad++;
    else porClave.set(t.barrioClave, { clave: t.barrioClave, nombre: t.barrio, cantidad: 1 });
  }
  return [...porClave.values()].sort((a, b) => b.cantidad - a.cantidad || a.nombre.localeCompare(b.nombre, "es"));
}
