import type { Metadata } from "next";
import Form from "next/form";
import Link from "next/link";
import { Suspense } from "react";
import { IconoLupa, IconoWhatsApp } from "@/components/Iconos";
import { TarjetaProducto } from "@/components/TarjetaProducto";
import { buscarProductos, obtenerCategorias, obtenerTaller, type FiltrosCatalogo } from "@/lib/api";
import { hayFiltros, leerFiltros, urlDelCatalogo, type ParametrosDeUrl } from "@/lib/catalogo";
import { linkWhatsApp } from "@/lib/whatsapp";

export const metadata: Metadata = {
  title: "Productos",
  description: "Cámaras de seguridad, alarmas, notebooks nuevas y reacondicionadas y PC armadas, con instalación en Rosario.",
};

/**
 * /productos: el catálogo, con filtros en la URL (?categoria=camaras&condicion=nuevo&q=dvr).
 *
 * La página se divide en dos:
 * - El título no depende de la URL: Next.js lo arma una sola vez y lo manda al instante.
 * - El catálogo SÍ depende de la URL (los filtros), así que va dentro de <Suspense>.
 *   Mientras se arma, se ve <CatalogoCargando />. Next 16 exige este Suspense para leer searchParams.
 */
export default function Pagina({ searchParams }: PageProps<"/productos">) {
  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Productos</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Seguridad y tecnología con instalación y garantía. Si no encontrás lo que buscás, consultanos: lo conseguimos.
      </p>

      <Suspense fallback={<CatalogoCargando />}>
        {searchParams.then((parametros) => (
          <Catalogo parametros={parametros} />
        ))}
      </Suspense>
    </main>
  );
}

async function Catalogo({ parametros }: { parametros: ParametrosDeUrl }) {
  const [categorias, taller] = await Promise.all([obtenerCategorias(), obtenerTaller()]);
  const filtros = leerFiltros(parametros, categorias);
  const productos = await buscarProductos(filtros);

  return (
    <>
      <div className="mt-10 space-y-5">
        <Buscador filtros={filtros} />

        <Opciones
          etiqueta="Categoría"
          opciones={[
            { texto: "Todo", url: urlDelCatalogo({ ...filtros, categoria: undefined }), activa: !filtros.categoria },
            ...categorias.map((c) => ({
              texto: c.nombre,
              url: urlDelCatalogo({ ...filtros, categoria: c.slug }),
              activa: filtros.categoria === c.slug,
            })),
          ]}
        />

        <Opciones
          etiqueta="Estado"
          opciones={[
            { texto: "Todos", url: urlDelCatalogo({ ...filtros, condicion: undefined }), activa: !filtros.condicion },
            { texto: "Nuevos", url: urlDelCatalogo({ ...filtros, condicion: "nuevo" }), activa: filtros.condicion === "nuevo" },
            {
              texto: "Reacondicionados",
              url: urlDelCatalogo({ ...filtros, condicion: "reacondicionado" }),
              activa: filtros.condicion === "reacondicionado",
            },
            {
              texto: "En oferta",
              url: urlDelCatalogo({ ...filtros, oferta: filtros.oferta ? undefined : true }),
              activa: !!filtros.oferta,
            },
          ]}
        />
      </div>

      <section aria-labelledby="resultados" className="mt-10">
        <div className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-2 border-b border-borde pb-3">
          {/* aria-live: quien usa lector de pantalla escucha cuántos resultados hay al cambiar un filtro. */}
          <h2 id="resultados" className="font-sans text-base font-normal not-italic text-suave" aria-live="polite">
            {textoResultados(productos.length, filtros)}
          </h2>
          {hayFiltros(filtros) && (
            <Link href="/productos" className="text-sm text-oro underline underline-offset-4 hover:text-oro-claro">
              Limpiar filtros
            </Link>
          )}
        </div>

        {productos.length > 0 ? (
          <div className="mt-6 grid grid-cols-2 gap-4 sm:gap-6 lg:grid-cols-4">
            {productos.map((p) => (
              <TarjetaProducto key={p.id} producto={p} whatsapp={taller.whatsapp} />
            ))}
          </div>
        ) : (
          <SinResultados whatsapp={taller.whatsapp} busqueda={filtros.q} />
        )}
      </section>
    </>
  );
}

/**
 * <Form> de Next: es un <form> común (anda aun sin JavaScript), pero con JavaScript cambia de página
 * sin recargar todo. Los filtros que ya estaban elegidos viajan en campos ocultos, para no perderlos al buscar.
 */
function Buscador({ filtros }: { filtros: FiltrosCatalogo }) {
  return (
    <Form action="/productos" role="search" className="max-w-xl">
      <label htmlFor="buscar-en-catalogo" className="sr-only">
        Buscar en el catálogo
      </label>
      <div className="flex items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
        {/* key: si cambia la búsqueda por un link (por ejemplo "Limpiar filtros"), el campo se vacía. */}
        <input
          key={filtros.q ?? ""}
          id="buscar-en-catalogo"
          name="q"
          type="search"
          defaultValue={filtros.q}
          maxLength={80}
          placeholder="¿Qué estás buscando?"
          className="w-full bg-transparent py-2.5 text-principal placeholder:text-suave/70 focus:outline-none"
        />
        {filtros.categoria && <input type="hidden" name="categoria" value={filtros.categoria} />}
        {filtros.condicion && <input type="hidden" name="condicion" value={filtros.condicion} />}
        <button type="submit" className="p-3 text-oro hover:text-oro-claro" aria-label="Buscar">
          <IconoLupa className="size-5" />
        </button>
      </div>
    </Form>
  );
}

type Opcion = { texto: string; url: string; activa: boolean };

/**
 * Una fila de filtros. Cada opción es un LINK, no un botón: se puede abrir en otra pestaña,
 * compartir, y Google lo sigue. aria-current marca la elegida para los lectores de pantalla.
 * scroll={false}: al cambiar de filtro la página no salta arriba de todo.
 */
function Opciones({ etiqueta, opciones }: { etiqueta: string; opciones: Opcion[] }) {
  return (
    // En el celular la etiqueta va arriba y las opciones bajan de renglón: así siempre se ve la elegida.
    <nav aria-label={`Filtrar por ${etiqueta.toLowerCase()}`} className="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-4">
      <span className="shrink-0 text-sm text-suave sm:w-20">{etiqueta}</span>
      <ul className="flex flex-wrap gap-2">
        {opciones.map((o) => (
          <li key={o.texto} className="shrink-0">
            <Link
              href={o.url}
              scroll={false}
              aria-current={o.activa ? "page" : undefined}
              className={
                o.activa
                  ? "boton-oro inline-block rounded-full px-4 py-1.5 text-sm font-semibold"
                  : "inline-block rounded-full border border-borde px-4 py-1.5 text-sm text-principal hover:border-oro hover:text-oro-claro"
              }
            >
              {o.texto}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}

function textoResultados(cantidad: number, filtros: FiltrosCatalogo): string {
  const productos = cantidad === 1 ? "1 producto" : `${cantidad} productos`;
  return filtros.q ? `${productos} para “${filtros.q}”` : productos;
}

function SinResultados({ whatsapp, busqueda }: { whatsapp: string; busqueda?: string }) {
  const mensaje = busqueda
    ? `Hola, estoy buscando: ${busqueda}. ¿Lo tienen o lo pueden conseguir?`
    : "Hola, quería consultar por un producto.";

  return (
    <div className="mt-6 rounded-xl border border-borde bg-superficie px-6 py-12 text-center">
      <p className="font-display text-2xl font-bold italic text-principal">No encontramos productos con esos filtros</p>
      <p className="mx-auto mt-3 max-w-md text-suave">
        Probá con otra búsqueda o escribinos: muchas cosas las conseguimos aunque no estén publicadas.
      </p>
      <div className="mt-6 flex flex-wrap justify-center gap-3">
        <a
          href={linkWhatsApp(whatsapp, mensaje)}
          className="inline-flex items-center gap-2 rounded-full bg-whatsapp px-5 py-2.5 font-semibold text-negro hover:brightness-110"
        >
          <IconoWhatsApp className="size-5" />
          Consultar por WhatsApp
        </a>
        <Link href="/productos" className="rounded-full border border-borde px-5 py-2.5 text-principal hover:border-oro">
          Ver todo el catálogo
        </Link>
      </div>
    </div>
  );
}

/** Lo que se ve mientras se arma el catálogo: la misma forma, en gris, para que nada salte al aparecer. */
function CatalogoCargando() {
  return (
    <div aria-hidden className="mt-10 animate-pulse">
      <div className="h-12 max-w-xl rounded-full bg-superficie" />
      <div className="mt-5 h-8 max-w-lg rounded-full bg-superficie" />
      <div className="mt-5 h-8 max-w-sm rounded-full bg-superficie" />
      <div className="mt-16 grid grid-cols-2 gap-4 sm:gap-6 lg:grid-cols-4">
        {Array.from({ length: 8 }, (_, i) => (
          <div key={i} className="aspect-[3/4] rounded-xl bg-superficie" />
        ))}
      </div>
    </div>
  );
}
