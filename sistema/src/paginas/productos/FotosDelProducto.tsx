import { useRef, useState } from "react";
import { useFotos, type DetalleDeProducto } from "../../api/productos";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { urlDeImagen } from "../../utilidades/imagenes";

const MAXIMO = 6;
const TAMANO_MAXIMO = 10 * 1024 * 1024;

/**
 * Las fotos del producto: subir (varias a la vez), ordenar y borrar. La primera es la principal.
 * El backend las achica y las endereza (las del celular vienen "acostadas").
 */
export function FotosDelProducto({ producto, soloLectura }: { producto: DetalleDeProducto; soloLectura: boolean }) {
  const { subir, borrar, ordenar } = useFotos(producto.id);
  const avisar = useAvisar();
  const entrada = useRef<HTMLInputElement>(null);
  const [subiendo, setSubiendo] = useState(0);
  const fotos = producto.fotos;
  const ocupado = subiendo > 0 || borrar.isPending || ordenar.isPending;

  async function subirTodas(archivos: FileList | null) {
    if (!archivos?.length) return;
    const lugar = MAXIMO - fotos.length;
    const elegidos = [...archivos].slice(0, lugar);
    if (archivos.length > lugar) avisar(`Solo entran ${lugar} fotos más (máximo ${MAXIMO})`, "error");
    for (const archivo of elegidos) {
      if (archivo.size > TAMANO_MAXIMO) {
        avisar(`${archivo.name}: pesa más de 10 MB`, "error");
        continue;
      }
      setSubiendo((n) => n + 1);
      try {
        await subir.mutateAsync(archivo);
      } catch (e) {
        avisar(`${archivo.name}: ${e instanceof Error ? e.message : "no se pudo subir"}`, "error");
      } finally {
        setSubiendo((n) => n - 1);
      }
    }
    if (entrada.current) entrada.current.value = "";
  }

  function mover(desde: number, hacia: number) {
    const orden = fotos.map((f) => f.id);
    const [foto] = orden.splice(desde, 1);
    orden.splice(hacia, 0, foto);
    ordenar.mutate(orden, { onError: (e) => avisar(e.message, "error") });
  }

  return (
    <section className="rounded-xl border border-borde bg-superficie p-5">
      <h2 className="text-2xl">Fotos</h2>
      <p className="mt-1 text-sm text-suave">Hasta {MAXIMO}. La primera es la principal. JPG o PNG, hasta 10 MB.</p>

      {fotos.length === 0 && producto.imagenHeredada && (
        <div className="mt-3 flex items-center gap-3 text-sm text-suave">
          <img src={urlDeImagen(producto.imagenHeredada)} alt="" className="size-14 rounded-lg border border-borde object-cover" />
          Ahora usa la imagen de ejemplo. Al subir una foto, se reemplaza.
        </div>
      )}

      {fotos.length > 0 && (
        <ul className="mt-3 grid grid-cols-3 gap-2">
          {fotos.map((f, i) => (
            <li key={f.id} className="relative">
              <img
                src={urlDeImagen(f.url)}
                alt={`Foto ${i + 1}`}
                className={`aspect-square w-full rounded-lg border bg-white object-cover ${i === 0 ? "border-oro" : "border-borde"}`}
              />
              {i === 0 && <span className="absolute top-1 left-1 rounded bg-oro px-1.5 text-[0.65rem] font-semibold text-negro">Principal</span>}
              {!soloLectura && (
                <div className="mt-1 flex justify-between text-xs">
                  <button type="button" disabled={i === 0 || ocupado} onClick={() => mover(i, i - 1)} className="px-1 disabled:opacity-30" aria-label={`Mover la foto ${i + 1} antes`}>
                    ←
                  </button>
                  <button
                    type="button"
                    disabled={ocupado}
                    onClick={() => borrar.mutate(f.id, { onError: (e) => avisar(e.message, "error") })}
                    className="px-1 text-peligro"
                    aria-label={`Borrar la foto ${i + 1}`}
                  >
                    Borrar
                  </button>
                  <button
                    type="button"
                    disabled={i === fotos.length - 1 || ocupado}
                    onClick={() => mover(i, i + 1)}
                    className="px-1 disabled:opacity-30"
                    aria-label={`Mover la foto ${i + 1} después`}
                  >
                    →
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}

      {!soloLectura && fotos.length < MAXIMO && (
        <div className="mt-4">
          <input
            ref={entrada}
            id="subir-fotos"
            type="file"
            accept="image/jpeg,image/png"
            multiple
            className="sr-only"
            onChange={(e) => subirTodas(e.target.files)}
          />
          <label
            htmlFor="subir-fotos"
            className={`inline-block cursor-pointer rounded-full border border-borde px-5 py-2 text-sm font-semibold hover:border-oro ${ocupado ? "pointer-events-none opacity-60" : ""}`}
          >
            {subiendo > 0 ? `Subiendo ${subiendo}…` : "+ Agregar fotos"}
          </label>
        </div>
      )}
    </section>
  );
}
