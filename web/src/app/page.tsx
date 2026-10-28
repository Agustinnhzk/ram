import Image from "next/image";
import Link from "next/link";
import { CarruselPromociones } from "@/components/CarruselPromociones";
import { IconoDomicilio, IconoEscudo, IconoNotebook } from "@/components/Iconos";
import { TarjetaProducto } from "@/components/TarjetaProducto";
import { obtenerProductosDestacados, obtenerPromociones, obtenerTaller } from "@/lib/api";
import { linkWhatsApp } from "@/lib/whatsapp";

const servicios = [
  {
    id: "reparacion",
    titulo: "Reparación de notebooks y PC",
    descripcion: "Diagnóstico y reparación de tu equipo, con un link para seguir el estado de la reparación desde el celular.",
    detalles: ["A domicilio o en el local", "Retiro y entrega", "Sin costo si no se logra reparar"],
    enlace: { href: "/servicios#reparacion", texto: "Ver servicio técnico" },
  },
  {
    id: "seguridad",
    titulo: "Cámaras y alarmas",
    descripcion: "Kits y equipos DSC, X28, Garnet y Hikvision, con stock para llevar en el momento.",
    detalles: ["Interior y exterior", "Inalámbricas y cableadas", "Asesoramiento para elegir"],
    enlace: { href: "/productos?categoria=alarmas", texto: "Ver cámaras y alarmas" },
  },
  {
    id: "instalaciones",
    titulo: "Instalación y monitoreo",
    descripcion: "Instalamos tu sistema en casa o en tu negocio y te dejamos controlándolo desde el celular.",
    detalles: ["Hogar y empresa", "Alertas en el celular", "Abono de monitoreo mensual"],
    enlace: { href: "/servicios#instalaciones", texto: "Ver instalaciones" },
  },
  {
    id: "equipos",
    titulo: "Notebooks, PC y armado a medida",
    descripcion: "Equipos nuevos y reacondicionados, y PCs armadas para jugar, trabajar o estudiar.",
    detalles: ["Garantía incluida", "Envío gratis en Rosario", "Lista para usar"],
    enlace: { href: "/arma-tu-pc", texto: "Armar mi PC" },
  },
];

export default async function Inicio() {
  const [taller, promociones, productos] = await Promise.all([
    obtenerTaller(),
    obtenerPromociones(),
    obtenerProductosDestacados(),
  ]);

  return (
    <main>
      {/* Portada: el eslogan con el metal de los flyers, y el holograma como protagonista. */}
      <section className="relative overflow-hidden">
        <div className="mx-auto grid max-w-6xl items-center gap-8 px-4 pt-14 pb-10 sm:px-6 md:grid-cols-[1.15fr_1fr] md:pt-20">
          <div>
            <h1 className="texto-metalico text-[clamp(2.7rem,7vw,5rem)] font-black">{taller.eslogan}</h1>
            <p className="mt-6 max-w-[34rem] text-lg text-suave">
              Servicio técnico de notebooks y PC, cámaras y alarmas, y venta de equipos en {taller.ciudad}. Te
              atendemos en el local o a domicilio.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/productos" className="boton-oro rounded-full px-6 py-3 font-semibold hover:brightness-110">
                Ver productos
              </Link>
              <a
                href={linkWhatsApp(taller.whatsapp, "Hola, quería hacer una consulta.")}
                className="rounded-full border border-oro/70 px-6 py-3 font-semibold text-oro-claro hover:border-oro-claro"
              >
                Escribinos por WhatsApp
              </a>
            </div>
          </div>

          <div className="escenario-holograma relative mx-auto w-full max-w-md">
            <div className="brillo-holograma absolute inset-x-[8%] bottom-[2%] h-[62%] rounded-full blur-2xl" aria-hidden />
            <Image
              src="/marca/holograma.png"
              alt="Módulo de memoria RAM dorado sobre un pedestal luminoso, emblema de R.A.M."
              width={510}
              height={390}
              fetchPriority="high"
              loading="eager"
              className="relative h-auto w-full [mask-image:radial-gradient(closest-side,black_62%,transparent_100%)]"
            />
          </div>
        </div>

        <ul className="mx-auto grid max-w-6xl gap-px border-y border-borde bg-borde px-0 sm:grid-cols-3">
          {[
            { Icono: IconoNotebook, texto: "Servicio técnico de PC y notebooks" },
            { Icono: IconoEscudo, texto: "Cámaras de seguridad y alarmas" },
            { Icono: IconoDomicilio, texto: "Atención a domicilio" },
          ].map(({ Icono, texto }) => (
            <li key={texto} className="flex items-center gap-3 bg-fondo px-6 py-4 text-principal">
              <Icono className="size-7 shrink-0 text-oro" />
              {texto}
            </li>
          ))}
        </ul>
      </section>

      <section className="mx-auto mt-20 max-w-6xl px-4 sm:px-6" aria-labelledby="titulo-promociones">
        <h2 id="titulo-promociones" className="text-3xl text-principal">
          Promociones
        </h2>
        <div className="mt-6">
          <CarruselPromociones promociones={promociones} />
        </div>
      </section>

      {/* Qué hacemos: filas separadas por líneas doradas, como los paneles de los flyers. */}
      <section className="mx-auto mt-20 max-w-6xl px-4 sm:px-6" aria-labelledby="titulo-servicios">
        <h2 id="titulo-servicios" className="text-3xl text-principal">
          Qué hacemos
        </h2>
        <div className="mt-6 border-t border-oro/40">
          {servicios.map((s) => (
            <article
              key={s.id}
              className="grid gap-x-10 gap-y-3 border-b border-oro/40 py-8 md:grid-cols-[1fr_1.2fr_11rem] md:items-start"
            >
              <h3 className="text-2xl text-oro-claro">{s.titulo}</h3>
              <div>
                <p className="text-principal">{s.descripcion}</p>
                <ul className="mt-3 flex flex-wrap gap-2">
                  {s.detalles.map((d) => (
                    <li key={d} className="rounded-full border border-borde px-3 py-1 text-sm text-suave">
                      {d}
                    </li>
                  ))}
                </ul>
              </div>
              <Link
                href={s.enlace.href}
                className="self-center font-semibold text-oro underline decoration-oro/40 underline-offset-4 hover:text-oro-claro md:justify-self-end"
              >
                {s.enlace.texto}
              </Link>
            </article>
          ))}
        </div>
      </section>

      <section className="mx-auto mt-20 max-w-6xl px-4 sm:px-6" aria-labelledby="titulo-productos">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <h2 id="titulo-productos" className="text-3xl text-principal">
            Productos destacados
          </h2>
          <Link href="/productos" className="font-semibold text-oro underline decoration-oro/40 underline-offset-4 hover:text-oro-claro">
            Ver todos los productos
          </Link>
        </div>
        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {productos.map((p) => (
            <TarjetaProducto key={p.id} producto={p} whatsapp={taller.whatsapp} />
          ))}
        </div>
      </section>

      <section className="mx-auto mt-20 max-w-6xl px-4 sm:px-6" aria-labelledby="titulo-arma">
        <div className="grid items-center gap-8 overflow-hidden rounded-2xl border border-oro/50 bg-superficie md:grid-cols-[1.2fr_1fr]">
          <div className="p-8 md:p-12">
            <h2 id="titulo-arma" className="texto-metalico text-4xl">
              Armá la PC que necesitás
            </h2>
            <p className="mt-4 max-w-prose text-suave">
              Elegí componente por componente y te mostramos solo lo que es compatible con lo que ya elegiste. Después
              la armamos, la probamos y te la entregamos con el sistema instalado.
            </p>
            <Link href="/arma-tu-pc" className="boton-oro mt-7 inline-block rounded-full px-6 py-3 font-semibold hover:brightness-110">
              Armar mi PC
            </Link>
          </div>
          <Image
            src="/productos/pc-gamer.jpg"
            alt="PC gamer blanca con ventiladores de luces RGB"
            width={720}
            height={720}
            sizes="(min-width: 768px) 40vw, 100vw"
            className="h-full max-h-96 w-full object-cover"
          />
        </div>
      </section>
    </main>
  );
}
