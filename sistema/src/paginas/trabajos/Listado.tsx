import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router";
import { useSesion } from "../../api/sesion";
import { NOMBRE_DEL_TIPO_DE_TRABAJO, useTrabajos } from "../../api/trabajos";
import { IconoLupa, IconoUbicacion } from "../../componentes/Iconos";
import { urlDeImagen } from "../../utilidades/imagenes";
import { normalizar } from "../../utilidades/texto";

type Vista = "todos" | "publicados" | "borradores";

const VISTAS: { valor: Vista; texto: string }[] = [
  { valor: "todos", texto: "Todos" },
  { valor: "publicados", texto: "Publicados" },
  { valor: "borradores", texto: "Sin publicar" },
];

const fecha = new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeZone: "UTC" });

/** /trabajos — los trabajos realizados que se muestran en la tienda (ADR-018). */
export function Listado() {
  const { data: sesion } = useSesion();
  const { data: trabajos, isPending, isError, error } = useTrabajos();
  const [busqueda, setBusqueda] = useState("");
  const [vista, setVista] = useState<Vista>("todos");
  const navegar = useNavigate();
  const esDueno = sesion?.rol === "DUENO";

  const visibles = useMemo(() => {
    const palabras = normalizar(busqueda).split(/\s+/).filter(Boolean);
    return (trabajos ?? []).filter((t) => {
      if (vista === "publicados" && !t.publicado) return false;
      if (vista === "borradores" && t.publicado) return false;
      const texto = normalizar(`${t.titulo} ${t.barrio} ${NOMBRE_DEL_TIPO_DE_TRABAJO[t.tipo]}`);
      return palabras.every((palabra) => texto.includes(palabra));
    });
  }, [trabajos, busqueda, vista]);

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Trabajos</h1>
          <p className="mt-2 max-w-prose text-suave">
            Casos reales que se muestran en la tienda, con el barrio donde se hicieron. Ayudan a que te encuentren en Google
            y le dan confianza a quien llega.
          </p>
        </div>
        {esDueno && (
          <Link to="/trabajos/nuevo" className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
            + Nuevo trabajo
          </Link>
        )}
      </div>

      <div className="mt-6 flex min-w-60 items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
        <IconoLupa className="size-5 text-suave" />
        <label htmlFor="buscar-trabajo" className="sr-only">
          Buscar trabajo
        </label>
        <input
          id="buscar-trabajo"
          type="search"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          placeholder="Buscar por título o barrio…"
          className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
        />
      </div>
      <div className="mt-3 flex flex-wrap gap-2" role="group" aria-label="Qué trabajos ver">
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
          No se pudieron cargar los trabajos: {error.message}
        </p>
      )}
      {trabajos && trabajos.length === 0 && (
        <div className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center">
          <p className="font-semibold text-principal">Todavía no cargaste ningún trabajo</p>
          <p className="mx-auto mt-2 max-w-md text-sm text-suave">
            Cargá uno por cada reparación o instalación que valga la pena mostrar: el problema, qué hiciste, el barrio y un par
            de fotos. Con 4 o 5 líneas reales alcanza.
          </p>
        </div>
      )}
      {trabajos && trabajos.length > 0 && visibles.length === 0 && (
        <p className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center">No hay trabajos que coincidan.</p>
      )}

      {visibles.length > 0 && (
        <ul className="mt-6 divide-y divide-borde rounded-xl border border-borde">
          {visibles.map((t) => (
            <li key={t.id}>
              <button
                type="button"
                onClick={() => navegar(`/trabajos/${t.id}`)}
                className="flex w-full items-center gap-4 px-4 py-3 text-left hover:bg-superficie"
              >
                <span className="grid size-14 shrink-0 place-items-center overflow-hidden rounded-lg border border-borde bg-fondo">
                  {t.miniatura ? (
                    <img src={urlDeImagen(t.miniatura)} alt="" loading="lazy" className="size-full object-cover" />
                  ) : (
                    <IconoUbicacion className="size-7 text-suave" />
                  )}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate font-semibold text-principal">{t.titulo}</span>
                  <span className="flex flex-wrap gap-x-3 text-xs text-suave">
                    <span>{t.barrio}</span>
                    <span>· {NOMBRE_DEL_TIPO_DE_TRABAJO[t.tipo]}</span>
                    {t.aDomicilio && <span>· A domicilio</span>}
                    <span>· {fecha.format(new Date(t.fecha))}</span>
                  </span>
                </span>
                <span className={`shrink-0 text-xs font-semibold ${t.publicado ? "text-exito" : "text-aviso"}`}>
                  {t.publicado ? "Publicado" : "Sin publicar"}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
