import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { BotonAgregarAlCarrito } from "@/components/BotonAgregarAlCarrito";
import { EstadoDeStock, sinStock } from "@/components/EstadoDeStock";
import { IconoCheck, IconoDomicilio, IconoEscudo, IconoWhatsApp } from "@/components/Iconos";
import { GaleriaDeFotos } from "@/components/GaleriaDeFotos";
import { PrecioConOferta } from "@/components/PrecioConOferta";
import { TarjetaProducto } from "@/components/TarjetaProducto";
import { obtenerCategorias, obtenerProducto, obtenerRelacionados, obtenerSlugsDeProductos, obtenerTaller } from "@/lib/api";
import { urlDelCatalogo } from "@/lib/catalogo";
import { linkWhatsApp } from "@/lib/whatsapp";

/**
 * /productos/[slug]: la página de un producto. [slug] es la parte variable: /productos/kit-4-camaras-dvr
 *
 * generateStaticParams le dice a Next.js qué productos existen, y arma la página de cada uno
 * al compilar: llega como HTML ya hecho, que es lo más rápido posible y lo que mejor lee Google.
 * Un producto que se cargue después se arma en su primera visita y se guarda igual.
 */
export async function generateStaticParams() {
  const slugs = await obtenerSlugsDeProductos();
  return slugs.map((slug) => ({ slug }));
}

/** El título de la pestaña y lo que se ve al compartir el link por WhatsApp o en redes. */
export async function generateMetadata({ params }: PageProps<"/productos/[slug]">): Promise<Metadata> {
  const { slug } = await params;
  const producto = await obtenerProducto(slug);
  if (!producto) return { title: "Producto no encontrado" };

  return {
    title: producto.nombre,
    description: producto.resumen,
    openGraph: { title: producto.nombre, description: producto.resumen, images: producto.imagen ? [producto.imagen] : [] },
  };
}

export default async function Pagina({ params }: PageProps<"/productos/[slug]">) {
  const { slug } = await params;
  const producto = await obtenerProducto(slug);
  // Slug que no existe (link viejo, error de tipeo): página "no encontrado" con código 404.
  if (!producto) notFound();

  const [taller, categorias, relacionados] = await Promise.all([
    obtenerTaller(),
    obtenerCategorias(),
    obtenerRelacionados(producto),
  ]);
  const categoria = categorias.find((c) => c.slug === producto.categoria);
  const reacondicionado = producto.condicion === "reacondicionado";
  const parrafos = producto.descripcion.split("\n\n");

  return (
    <main className="mx-auto max-w-6xl px-4 py-10 sm:px-6 sm:py-14">
      {/* Migas de pan: dónde estás y cómo volver. */}
      <nav aria-label="Estás en" className="text-sm text-suave">
        <ol className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <li>
            <Link href="/productos" className="hover:text-oro-claro">
              Productos
            </Link>
          </li>
          {categoria && (
            <>
              <li aria-hidden>/</li>
              <li>
                <Link href={urlDelCatalogo({ categoria: categoria.slug })} className="hover:text-oro-claro">
                  {categoria.nombre}
                </Link>
              </li>
            </>
          )}
          <li aria-hidden>/</li>
          <li aria-current="page" className="text-principal">
            {producto.nombre}
          </li>
        </ol>
      </nav>

      <div className="mt-6 grid gap-8 md:grid-cols-2 md:gap-12">
        <GaleriaDeFotos
          imagenes={producto.imagenes}
          nombre={producto.nombre}
          etiqueta={reacondicionado ? "Reacondicionada" : producto.precioAnterior !== null ? "Oferta" : undefined}
        />

        <div>
          <h1 className="text-4xl text-principal sm:text-5xl">{producto.nombre}</h1>
          <p className="mt-4 text-lg text-suave">{producto.resumen}</p>

          <div className="mt-6 border-y border-borde py-5">
            {producto.precio !== null ? (
              <PrecioConOferta producto={producto} grande />
            ) : (
              <p className="text-principal">
                <span className="font-display text-2xl font-bold italic">Precio a consultar</span>
                <span className="mt-1 block text-sm text-suave">Te pasamos el precio actualizado y la disponibilidad por WhatsApp.</span>
              </p>
            )}

            <EstadoDeStock stock={producto.stock} className="mt-3" />
            {sinStock(producto.stock) && producto.precio !== null && (
              <p className="mt-2 text-sm text-suave">
                Lo podés comprar igual: queda reservado a tu nombre y te avisamos apenas llegue. Consultanos la demora por WhatsApp.
              </p>
            )}

            {/* Con precio: se puede agregar al carrito, y WhatsApp queda para las dudas. Sin precio: solo consulta. */}
            {producto.precio !== null && (
              <BotonAgregarAlCarrito
                texto={sinStock(producto.stock) ? "Reservar" : "Agregar al carrito"}
                className="mt-5"
                item={{
                  id: `producto:${producto.slug}`,
                  nombre: producto.nombre,
                  precioUnitario: producto.precio,
                  imagen: producto.miniatura,
                  enlace: `/productos/${producto.slug}`,
                  detalle: [],
                }}
              />
            )}
            <a
              href={linkWhatsApp(taller.whatsapp, `Hola, quería consultar por: ${producto.nombre}.`)}
              className={
                producto.precio !== null
                  ? "mt-4 inline-flex items-center gap-2 text-sm font-semibold text-oro underline underline-offset-4 hover:text-oro-claro"
                  : "mt-5 inline-flex items-center gap-2 rounded-full bg-whatsapp px-6 py-3 font-semibold text-negro hover:brightness-110"
              }
            >
              <IconoWhatsApp className={producto.precio !== null ? "size-4" : "size-5"} />
              {producto.precio !== null ? "¿Tenés dudas? Consultanos por WhatsApp" : "Consultar por WhatsApp"}
            </a>
          </div>

          <ul className="mt-6 space-y-3">
            {producto.garantiaMeses && (
              <li className="flex items-center gap-3 text-principal">
                <IconoEscudo className="size-6 shrink-0 text-oro" />
                Garantía de {producto.garantiaMeses} meses
              </li>
            )}
            {categoria?.conInstalacion && (
              <li className="flex items-center gap-3 text-principal">
                <IconoDomicilio className="size-6 shrink-0 text-oro" />
                Lo instalamos en tu casa o negocio en {taller.ciudad} y alrededores
              </li>
            )}
          </ul>
        </div>
      </div>

      <div className="mt-14 grid gap-10 md:grid-cols-2 md:gap-12">
        <section aria-labelledby="descripcion">
          <h2 id="descripcion" className="text-3xl text-principal">
            Descripción
          </h2>
          <div className="mt-4 space-y-4 text-suave">
            {parrafos.map((p, i) => (
              <p key={i}>{p}</p>
            ))}
          </div>
        </section>

        {producto.caracteristicas.length > 0 && (
          <section aria-labelledby="caracteristicas">
            <h2 id="caracteristicas" className="text-3xl text-principal">
              Características
            </h2>
            <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde bg-superficie">
              {producto.caracteristicas.map((c) => (
                <li key={c} className="flex items-center gap-3 px-4 py-3 text-principal">
                  <IconoCheck className="size-5 shrink-0 text-oro" />
                  {c}
                </li>
              ))}
            </ul>
          </section>
        )}
      </div>

      {relacionados.length > 0 && (
        <section aria-labelledby="relacionados" className="mt-16">
          <h2 id="relacionados" className="text-3xl text-principal">
            También te puede interesar
          </h2>
          <div className="mt-6 grid grid-cols-2 gap-4 sm:gap-6 lg:grid-cols-4">
            {relacionados.map((p) => (
              <TarjetaProducto key={p.id} producto={p} whatsapp={taller.whatsapp} />
            ))}
          </div>
        </section>
      )}
    </main>
  );
}
