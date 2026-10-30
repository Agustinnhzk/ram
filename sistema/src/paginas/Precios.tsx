import { useMemo, useState } from "react";
import { usePrecios, type ProductoConPrecio } from "../api/precios";
import { useSesion } from "../api/sesion";
import { IconoLupa, IconoPorcentaje } from "../componentes/Iconos";
import { normalizar } from "../utilidades/texto";
import { DialogoAumento } from "./precios/DialogoAumento";
import { DialogoHistorial } from "./precios/DialogoHistorial";
import { FilaDePrecio } from "./precios/FilaDePrecio";
import { TarjetaArmado } from "./precios/TarjetaArmado";

/**
 * /precios — todos los productos con su precio, agrupados por categoría.
 * El dueño los edita en la misma tabla; el resto del personal los ve (ADR-008).
 */
export function Precios() {
  const { data: sesion } = useSesion();
  const { data: productos, isPending, isError, error, refetch } = usePrecios();
  const [busqueda, setBusqueda] = useState("");
  const [categoria, setCategoria] = useState("");
  const [historialDe, setHistorialDe] = useState<ProductoConPrecio | null>(null);
  const [aumentoAbierto, setAumentoAbierto] = useState(false);

  const esDueno = sesion?.rol === "DUENO";

  const categorias = useMemo(() => {
    const vistas = new Map<string, string>();
    productos?.forEach((p) => vistas.set(p.categoria.slug, p.categoria.nombre));
    return [...vistas].map(([slug, nombre]) => ({ slug, nombre }));
  }, [productos]);

  // Filtrar y agrupar: se recalcula solo cuando cambian los datos o los filtros.
  const grupos = useMemo(() => {
    const palabras = normalizar(busqueda).split(/\s+/).filter(Boolean);
    const filtrados = (productos ?? []).filter(
      (p) =>
        (!categoria || p.categoria.slug === categoria) &&
        palabras.every((palabra) => normalizar(p.nombre).includes(palabra)),
    );
    return categorias
      .map((c) => ({ ...c, productos: filtrados.filter((p) => p.categoria.slug === c.slug) }))
      .filter((g) => g.productos.length > 0);
  }, [productos, categorias, busqueda, categoria]);

  const sinPrecio = productos?.filter((p) => p.precio === null).length ?? 0;

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Precios</h1>
          <p className="mt-2 text-suave">
            {esDueno
              ? "Escribí el precio nuevo y apretá Enter. Si lo dejás vacío, pasa a “a consultar”."
              : "Solo el dueño puede cambiar precios. Vos podés verlos y consultar su historial."}
          </p>
        </div>
        <button
          type="button"
          onClick={() => setAumentoAbierto(true)}
          disabled={!productos?.length}
          className="boton-oro inline-flex items-center gap-2 rounded-full px-5 py-2.5 font-semibold hover:brightness-110 disabled:opacity-50"
        >
          <IconoPorcentaje className="size-5" />
          Aumento por porcentaje
        </button>
      </div>

      <TarjetaArmado esDueno={esDueno} />

      <div className="mt-6 flex flex-wrap gap-3">
        <div className="flex min-w-60 flex-1 items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
          <IconoLupa className="size-5 text-suave" />
          <label htmlFor="buscar-producto" className="sr-only">
            Buscar producto
          </label>
          <input
            id="buscar-producto"
            type="search"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar producto…"
            className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
          />
        </div>
        <label htmlFor="filtrar-categoria" className="sr-only">
          Categoría
        </label>
        <select
          id="filtrar-categoria"
          value={categoria}
          onChange={(e) => setCategoria(e.target.value)}
          className="rounded-full border border-borde bg-superficie px-4 py-2 text-principal focus:border-oro focus:outline-none"
        >
          <option value="">Todas las categorías</option>
          {categorias.map((c) => (
            <option key={c.slug} value={c.slug}>
              {c.nombre}
            </option>
          ))}
        </select>
      </div>

      {productos && (
        <p className="mt-4 text-sm text-suave">
          {productos.length} productos{sinPrecio > 0 && ` · ${sinPrecio} sin precio (a consultar)`}
        </p>
      )}

      {isPending && <div aria-hidden className="mt-6 h-96 animate-pulse rounded-xl bg-superficie" />}

      {isError && (
        <div role="alert" className="mt-6 rounded-xl border border-peligro/60 bg-peligro/10 p-5">
          <p>No se pudieron cargar los precios: {error.message}</p>
          <button type="button" onClick={() => refetch()} className="mt-2 text-oro underline underline-offset-4">
            Reintentar
          </button>
        </div>
      )}

      {productos && grupos.length === 0 && <p className="mt-10 text-center text-suave">No hay productos que coincidan con la búsqueda.</p>}

      {grupos.map((grupo) => (
        <section key={grupo.slug} aria-labelledby={`grupo-${grupo.slug}`} className="mt-8">
          <h2 id={`grupo-${grupo.slug}`} className="text-2xl text-oro-claro">
            {grupo.nombre} <span className="font-sans text-base font-normal not-italic text-suave">({grupo.productos.length})</span>
          </h2>
          {/* relative: el texto oculto "Acciones" (sr-only, position absolute) queda adentro del scroll y no ensancha la página. */}
          <div className="relative mt-3 overflow-x-auto rounded-xl border border-borde">
            <table className="w-full min-w-[34rem] text-left text-sm">
              <thead className="bg-superficie text-xs tracking-wide text-suave uppercase">
                <tr>
                  <th scope="col" className="px-4 py-3 font-medium">
                    Producto
                  </th>
                  <th scope="col" className="w-64 px-4 py-3 font-medium">
                    Precio
                  </th>
                  <th scope="col" className="w-28 px-4 py-3 font-medium">
                    <span className="sr-only">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-borde">
                {grupo.productos.map((p) => (
                  // La key incluye el precio: cuando el servidor confirma un precio nuevo,
                  // la fila se "reinicia" con ese valor y el campo deja de estar modificado.
                  <FilaDePrecio
                    key={`${p.id}-${p.precio}`}
                    producto={p}
                    editable={esDueno}
                    alVerHistorial={() => setHistorialDe(p)}
                  />
                ))}
              </tbody>
            </table>
          </div>
        </section>
      ))}

      <DialogoAumento
        abierto={aumentoAbierto}
        alCerrar={() => setAumentoAbierto(false)}
        categorias={categorias}
        puedeAplicar={esDueno}
      />
      <DialogoHistorial producto={historialDe} alCerrar={() => setHistorialDe(null)} />
    </>
  );
}
