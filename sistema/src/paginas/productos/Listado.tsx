import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router";
import { useProductos } from "../../api/productos";
import { useSesion } from "../../api/sesion";
import { IconoCaja, IconoLupa } from "../../componentes/Iconos";
import { formatearPrecio } from "../../utilidades/formato";
import { urlDeImagen } from "../../utilidades/imagenes";
import { NOMBRE_DEL_TIPO } from "../../utilidades/productos";
import { normalizar } from "../../utilidades/texto";
import { DialogoCategorias } from "./DialogoCategorias";

type Vista = "todos" | "tienda" | "componentes" | "ofertas" | "ocultos";

const VISTAS: { valor: Vista; texto: string }[] = [
  { valor: "todos", texto: "Todos" },
  { valor: "tienda", texto: "En la tienda" },
  { valor: "componentes", texto: "Componentes de Armá tu PC" },
  { valor: "ofertas", texto: "En oferta" },
  { valor: "ocultos", texto: "Ocultos" },
];

/** /productos — el catálogo completo para cargar y editar (ADR-013). El precio rápido sigue en /precios. */
export function Listado() {
  const { data: sesion } = useSesion();
  const { data: productos, isPending, isError, error } = useProductos();
  const [busqueda, setBusqueda] = useState("");
  const [vista, setVista] = useState<Vista>("todos");
  const [categoriasAbierto, setCategoriasAbierto] = useState(false);
  const navegar = useNavigate();
  const esDueno = sesion?.rol === "DUENO";

  const visibles = useMemo(() => {
    const palabras = normalizar(busqueda).split(/\s+/).filter(Boolean);
    return (productos ?? []).filter((p) => {
      if (vista === "tienda" && !p.publicado) return false;
      if (vista === "componentes" && !p.tipoComponente) return false;
      if (vista === "ofertas" && !p.enOferta) return false;
      if (vista === "ocultos" && p.publicado) return false;
      const texto = normalizar(`${p.nombre} ${p.categoria.nombre}`);
      return palabras.every((palabra) => texto.includes(palabra));
    });
  }, [productos, busqueda, vista]);

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Productos</h1>
          <p className="mt-2 text-suave">
            {esDueno ? "Cargá y editá productos, componentes, fotos y ofertas." : "Solo el dueño puede cargar y editar productos."}
          </p>
        </div>
        {esDueno && (
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => setCategoriasAbierto(true)}
              className="rounded-full border border-borde px-5 py-2.5 text-sm font-semibold hover:border-oro"
            >
              Categorías
            </button>
            <Link to="/productos/nuevo" className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
              + Nuevo producto
            </Link>
          </div>
        )}
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-3">
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
            placeholder="Buscar por nombre o categoría…"
            className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
          />
        </div>
      </div>
      <div className="mt-3 flex flex-wrap gap-2" role="group" aria-label="Qué productos ver">
        {VISTAS.map((v) => (
          <button
            key={v.valor}
            type="button"
            aria-pressed={vista === v.valor}
            onClick={() => setVista(v.valor)}
            className={`rounded-full border px-3 py-1 text-xs ${vista === v.valor ? "border-oro bg-oro/15 text-oro-claro" : "border-borde text-suave hover:border-oro"}`}
          >
            {v.texto}
          </button>
        ))}
      </div>

      {isPending && <div aria-hidden className="mt-6 h-80 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-6 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudieron cargar los productos: {error.message}
        </p>
      )}
      {productos && visibles.length === 0 && (
        <p className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center">No hay productos que coincidan.</p>
      )}

      {visibles.length > 0 && (
        <ul className="mt-6 divide-y divide-borde rounded-xl border border-borde">
          {visibles.map((p) => (
            <li key={p.id}>
              <button
                type="button"
                onClick={() => navegar(`/productos/${p.id}`)}
                className="flex w-full items-center gap-4 px-4 py-3 text-left hover:bg-superficie"
              >
                <span className="grid size-14 shrink-0 place-items-center overflow-hidden rounded-lg border border-borde bg-white">
                  {p.miniatura ? (
                    <img src={urlDeImagen(p.miniatura)} alt="" loading="lazy" className="size-full object-cover" />
                  ) : (
                    <IconoCaja className="size-7 text-suave" />
                  )}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate font-semibold text-principal">{p.nombre}</span>
                  <span className="flex flex-wrap gap-x-3 text-xs text-suave">
                    <span>{p.categoria.nombre}</span>
                    {p.tipoComponente && <span>· {NOMBRE_DEL_TIPO[p.tipoComponente]}</span>}
                    {!p.publicado && <span className="text-aviso">· Oculto en la tienda</span>}
                  </span>
                </span>
                <span className="shrink-0 text-right">
                  {p.enOferta && p.precio !== null && (
                    <span className="numeros block text-xs text-suave line-through">{formatearPrecio(p.precio)}</span>
                  )}
                  <span className="numeros block font-semibold text-principal">{formatearPrecio(p.precioVigente)}</span>
                  {p.enOferta && <span className="text-xs font-semibold text-oro">Oferta</span>}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}

      <DialogoCategorias abierto={categoriasAbierto} alCerrar={() => setCategoriasAbierto(false)} />
    </>
  );
}
