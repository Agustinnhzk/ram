import { useState, type FormEvent } from "react";
import { Link } from "react-router";
import { useAccionesDeBanners, useBanners, useProductos, type Banner, type DatosDeBanner } from "../api/productos";
import { useSesion } from "../api/sesion";
import { ESTILO_DE_CAMPO } from "../componentes/Campo";
import { useAvisar } from "../componentes/contextoDeAvisos";
import { Dialogo } from "../componentes/Dialogo";
import { formatearPrecio } from "../utilidades/formato";
import { urlDeImagen } from "../utilidades/imagenes";
import { formatearDia } from "../utilidades/reparaciones";

const TAMANO_MAXIMO = 10 * 1024 * 1024;

/**
 * /promociones — los banners (flyers) del carrusel de la portada y los productos en oferta (ADR-013).
 * Las ofertas se cargan en cada producto; acá se ven todas juntas.
 */
export function Promociones() {
  const { data: sesion } = useSesion();
  const esDueno = sesion?.rol === "DUENO";
  const { data: banners, isPending } = useBanners();
  const { data: productos } = useProductos();
  const acciones = useAccionesDeBanners();
  const avisar = useAvisar();
  const [editando, setEditando] = useState<Banner | "nuevo" | null>(null);
  const enOferta = productos?.filter((p) => p.enOferta) ?? [];

  function mover(desde: number, hacia: number) {
    if (!banners) return;
    const orden = banners.map((b) => b.id);
    const [id] = orden.splice(desde, 1);
    orden.splice(hacia, 0, id);
    acciones.ordenar.mutate(orden, { onError: (e) => avisar(e.message, "error") });
  }

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Promociones</h1>
          <p className="mt-2 text-suave">Los flyers del inicio de la tienda, en el orden en que se ven.</p>
        </div>
        {esDueno && (
          <button type="button" onClick={() => setEditando("nuevo")} className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
            + Nuevo banner
          </button>
        )}
      </div>

      {isPending && <div aria-hidden className="mt-6 h-64 animate-pulse rounded-xl bg-superficie" />}
      {banners && banners.length === 0 && (
        <p className="mt-6 rounded-xl border border-dashed border-borde px-6 py-12 text-center">Todavía no hay banners.</p>
      )}
      {banners && banners.length > 0 && (
        <ul className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {banners.map((b, i) => (
            <li key={b.id} className="flex flex-col overflow-hidden rounded-xl border border-borde bg-superficie">
              <div className="relative">
                <img src={urlDeImagen(b.imagen)} alt={b.textoAlternativo} className="aspect-square w-full bg-fondo object-cover" />
                <span
                  className={`absolute top-2 left-2 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                    b.visibleHoy ? "bg-exito text-negro" : "bg-negro/80 text-suave"
                  }`}
                >
                  {b.visibleHoy ? "Se ve hoy" : b.activo ? "Fuera de fecha" : "Apagado"}
                </span>
              </div>
              <div className="flex flex-1 flex-col p-3 text-sm">
                <p className="line-clamp-2 text-principal">{b.textoAlternativo}</p>
                <p className="mt-1 truncate text-xs text-suave">Lleva a {b.enlace}</p>
                {(b.desde || b.hasta) && (
                  <p className="mt-1 text-xs text-suave">
                    {b.desde ? `Desde el ${formatearDia(b.desde)}` : ""} {b.hasta ? `hasta el ${formatearDia(b.hasta)}` : ""}
                  </p>
                )}
                {esDueno && (
                  <div className="mt-auto flex items-center justify-between pt-3">
                    <div className="flex gap-1">
                      <button type="button" disabled={i === 0 || acciones.ordenar.isPending} onClick={() => mover(i, i - 1)} className="rounded border border-borde px-2 disabled:opacity-30" aria-label="Mover antes">
                        ←
                      </button>
                      <button
                        type="button"
                        disabled={i === banners.length - 1 || acciones.ordenar.isPending}
                        onClick={() => mover(i, i + 1)}
                        className="rounded border border-borde px-2 disabled:opacity-30"
                        aria-label="Mover después"
                      >
                        →
                      </button>
                    </div>
                    <button type="button" onClick={() => setEditando(b)} className="text-oro underline underline-offset-4">
                      Editar
                    </button>
                  </div>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      <section className="mt-12">
        <h2 className="text-3xl">Productos en oferta</h2>
        <p className="mt-1 text-sm text-suave">La oferta se carga en cada producto (Productos → el producto → Oferta).</p>
        {enOferta.length === 0 ? (
          <p className="mt-4 text-suave">No hay productos en oferta.</p>
        ) : (
          <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde">
            {enOferta.map((p) => (
              <li key={p.id}>
                <Link to={`/productos/${p.id}`} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 hover:bg-superficie">
                  <span>{p.nombre}</span>
                  <span className="numeros text-sm">
                    <span className="text-suave line-through">{formatearPrecio(p.precio)}</span>{" "}
                    <strong className="text-principal">{formatearPrecio(p.precioVigente)}</strong>
                    {p.ofertaHasta && <span className="text-suave"> · hasta el {formatearDia(p.ofertaHasta)}</span>}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      {editando && <DialogoBanner banner={editando === "nuevo" ? null : editando} alCerrar={() => setEditando(null)} />}
    </>
  );
}

function DialogoBanner({ banner, alCerrar }: { banner: Banner | null; alCerrar: () => void }) {
  const acciones = useAccionesDeBanners();
  const avisar = useAvisar();
  const [datos, setDatos] = useState<DatosDeBanner>({
    textoAlternativo: banner?.textoAlternativo ?? "",
    enlace: banner?.enlace ?? "/productos",
    activo: banner?.activo ?? true,
    desde: banner?.desde ?? null,
    hasta: banner?.hasta ?? null,
  });
  const [imagen, setImagen] = useState<File | null>(null);
  const [confirmarBorrado, setConfirmarBorrado] = useState(false);
  const ocupado = acciones.crear.isPending || acciones.editar.isPending || acciones.cambiarImagen.isPending || acciones.borrar.isPending;

  async function guardar(evento: FormEvent) {
    evento.preventDefault();
    if (imagen && imagen.size > TAMANO_MAXIMO) return avisar("La imagen pesa más de 10 MB", "error");
    try {
      if (!banner) {
        if (!imagen) return avisar("Elegí la imagen del banner", "error");
        await acciones.crear.mutateAsync({ datos, imagen });
      } else {
        await acciones.editar.mutateAsync({ id: banner.id, datos });
        if (imagen) await acciones.cambiarImagen.mutateAsync({ id: banner.id, imagen });
      }
      avisar(banner ? "Banner guardado" : "Banner creado");
      alCerrar();
    } catch (e) {
      avisar(e instanceof Error ? e.message : "No se pudo guardar", "error");
    }
  }

  return (
    <Dialogo abierto alCerrar={alCerrar} titulo={banner ? "Editar banner" : "Nuevo banner"} ancho="max-w-lg">
      <form onSubmit={guardar} className="space-y-4">
        <div>
          <label htmlFor="imagen-banner" className="text-sm text-suave">
            Imagen {banner && "(opcional: solo si la querés cambiar)"}
          </label>
          <input
            id="imagen-banner"
            type="file"
            accept="image/jpeg,image/png"
            onChange={(e) => setImagen(e.target.files?.[0] ?? null)}
            className="mt-1 block w-full text-sm"
          />
          <p className="mt-1 text-xs text-suave">Cuadrada, idealmente 1080 × 1080. JPG o PNG.</p>
        </div>
        <div>
          <label htmlFor="texto-banner" className="text-sm text-suave">
            Qué dice el banner (para quien no ve la imagen)
          </label>
          <textarea
            id="texto-banner"
            required
            maxLength={200}
            rows={2}
            value={datos.textoAlternativo}
            onChange={(e) => setDatos({ ...datos, textoAlternativo: e.target.value })}
            className={`mt-1 ${ESTILO_DE_CAMPO}`}
          />
        </div>
        <div>
          <label htmlFor="enlace-banner" className="text-sm text-suave">
            A dónde lleva al tocarlo
          </label>
          <input
            id="enlace-banner"
            value={datos.enlace}
            onChange={(e) => setDatos({ ...datos, enlace: e.target.value })}
            placeholder="/productos?categoria=camaras"
            className={`mt-1 ${ESTILO_DE_CAMPO}`}
          />
          <p className="mt-1 text-xs text-suave">Una página de la tienda: /productos, /productos?oferta=si, /arma-tu-pc, /servicios…</p>
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          <div>
            <label htmlFor="desde-banner" className="text-sm text-suave">
              Desde (opcional)
            </label>
            <input id="desde-banner" type="date" value={datos.desde ?? ""} onChange={(e) => setDatos({ ...datos, desde: e.target.value || null })} className={`mt-1 ${ESTILO_DE_CAMPO}`} />
          </div>
          <div>
            <label htmlFor="hasta-banner" className="text-sm text-suave">
              Hasta (opcional)
            </label>
            <input id="hasta-banner" type="date" value={datos.hasta ?? ""} onChange={(e) => setDatos({ ...datos, hasta: e.target.value || null })} className={`mt-1 ${ESTILO_DE_CAMPO}`} />
          </div>
        </div>
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={datos.activo} onChange={(e) => setDatos({ ...datos, activo: e.target.checked })} />
          Activo (se muestra en la tienda)
        </label>

        <div className="flex flex-wrap items-center justify-between gap-3 border-t border-borde pt-4">
          {banner ? (
            confirmarBorrado ? (
              <span className="flex items-center gap-2 text-sm">
                ¿Borrarlo?
                <button
                  type="button"
                  disabled={ocupado}
                  onClick={() =>
                    acciones.borrar.mutate(banner.id, {
                      onSuccess: () => {
                        avisar("Banner borrado");
                        alCerrar();
                      },
                      onError: (e) => avisar(e.message, "error"),
                    })
                  }
                  className="font-semibold text-peligro"
                >
                  Sí, borrar
                </button>
                <button type="button" onClick={() => setConfirmarBorrado(false)} className="text-suave">
                  No
                </button>
              </span>
            ) : (
              <button type="button" onClick={() => setConfirmarBorrado(true)} className="text-sm text-peligro">
                Borrar banner
              </button>
            )
          ) : (
            <span />
          )}
          <button type="submit" disabled={ocupado} className="boton-oro rounded-full px-5 py-2 font-semibold hover:brightness-110 disabled:opacity-50">
            {ocupado ? "Guardando…" : "Guardar"}
          </button>
        </div>
      </form>
    </Dialogo>
  );
}
