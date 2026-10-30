import { useState, type FormEvent, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router";
import {
  useCambiarPrecio,
  useCategorias,
  useCrearProducto,
  useEditarProducto,
  useProducto,
  type DetalleDeProducto,
  type Ficha,
  type FormatoMother,
  type Tecnicos,
  type TipoComponente,
  type TipoRam,
} from "../../api/productos";
import { useSesion } from "../../api/sesion";
import { ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { formatearPrecio, leerPrecio } from "../../utilidades/formato";
import { NOMBRE_DEL_TIPO } from "../../utilidades/productos";
import { URL_TIENDA } from "../../utilidades/reparaciones";
import { FotosDelProducto } from "./FotosDelProducto";
import { OfertaDelProducto } from "./OfertaDelProducto";

const FICHA_VACIA: Ficha = {
  nombre: "",
  categoria: "",
  condicion: "NUEVO",
  resumen: "",
  descripcion: "",
  caracteristicas: [],
  garantiaMeses: null,
  publicado: true,
  tecnicos: null,
};

/** /productos/nuevo y /productos/:id — la ficha del producto; al editar, además, precio, oferta y fotos. */
export function Formulario() {
  const { id } = useParams();
  const { data: producto, isPending, isError, error } = useProducto(id);

  if (id && isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (id && isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar el producto: {error.message}
      </p>
    );
  }
  // key: si cambia de producto, el formulario arranca de cero con los datos nuevos.
  return <Contenido key={producto?.id ?? "nuevo"} producto={producto ?? null} />;
}

function Contenido({ producto }: { producto: DetalleDeProducto | null }) {
  const { data: sesion } = useSesion();
  const esDueno = sesion?.rol === "DUENO";

  return (
    <>
      <Link to="/productos" className="text-sm text-suave hover:text-oro-claro">
        ← Productos
      </Link>
      <div className="mt-2 flex flex-wrap items-end justify-between gap-3">
        <h1 className="texto-metalico text-4xl sm:text-5xl">{producto ? producto.nombre : "Nuevo producto"}</h1>
        {producto?.publicado && (
          <a href={`${URL_TIENDA}/productos/${producto.slug}`} target="_blank" rel="noreferrer" className="text-sm text-oro underline underline-offset-4">
            Ver en la tienda ↗
          </a>
        )}
      </div>
      {!esDueno && <p className="mt-2 text-suave">Solo el dueño puede cambiar productos: acá podés verlos.</p>}

      <div className="mt-8 grid items-start gap-6 lg:grid-cols-[1fr_22rem]">
        <FichaDelProducto producto={producto} soloLectura={!esDueno} />
        {producto && (
          <div className="space-y-6">
            <PrecioDelProducto producto={producto} soloLectura={!esDueno} />
            <OfertaDelProducto producto={producto} soloLectura={!esDueno} />
            <FotosDelProducto producto={producto} soloLectura={!esDueno} />
          </div>
        )}
      </div>
    </>
  );
}

function FichaDelProducto({ producto, soloLectura }: { producto: DetalleDeProducto | null; soloLectura: boolean }) {
  const { data: categorias } = useCategorias();
  const crear = useCrearProducto();
  const editar = useEditarProducto(producto?.id ?? "");
  const avisar = useAvisar();
  const navegar = useNavigate();
  const [ficha, setFicha] = useState<Ficha>(producto ?? { ...FICHA_VACIA, categoria: "" });
  const [caracteristicas, setCaracteristicas] = useState((producto?.caracteristicas ?? []).join("\n"));
  const [precio, setPrecio] = useState("");
  const enviando = crear.isPending || editar.isPending;

  const cambiar = <K extends keyof Ficha>(campo: K, valor: Ficha[K]) => setFicha((f) => ({ ...f, [campo]: valor }));

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    const datos: Ficha = { ...ficha, caracteristicas: caracteristicas.split("\n").map((c) => c.trim()).filter(Boolean) };
    if (producto) {
      editar.mutate(datos, { onSuccess: () => avisar("Producto guardado"), onError: (e) => avisar(e.message, "error") });
      return;
    }
    const precioInicial = precio.trim() ? leerPrecio(precio) : null;
    if (precio.trim() && precioInicial === null) {
      avisar("El precio tiene que ser un número de pesos, sin centavos", "error");
      return;
    }
    crear.mutate(
      { ...datos, precio: precioInicial },
      {
        onSuccess: ({ id }) => {
          avisar("Producto creado. Ahora podés agregarle fotos.");
          navegar(`/productos/${id}`, { replace: true });
        },
        onError: (e) => avisar(e.message, "error"),
      },
    );
  }

  return (
    <form onSubmit={guardar} className="rounded-xl border border-borde bg-superficie p-5 sm:p-6">
      <fieldset disabled={soloLectura} className="space-y-4">
        <Fila etiqueta="Nombre" id="nombre">
          <input id="nombre" required maxLength={150} value={ficha.nombre} onChange={(e) => cambiar("nombre", e.target.value)} className={ESTILO_DE_CAMPO} />
        </Fila>
        <div className="grid gap-4 sm:grid-cols-2">
          <Fila etiqueta="Categoría" id="categoria">
            <select id="categoria" required value={ficha.categoria} onChange={(e) => cambiar("categoria", e.target.value)} className={ESTILO_DE_CAMPO}>
              <option value="" disabled>
                Elegí una…
              </option>
              {categorias?.map((c) => (
                <option key={c.slug} value={c.slug}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </Fila>
          <Fila etiqueta="Estado" id="condicion">
            <select id="condicion" value={ficha.condicion} onChange={(e) => cambiar("condicion", e.target.value as Ficha["condicion"])} className={ESTILO_DE_CAMPO}>
              <option value="NUEVO">Nuevo</option>
              <option value="REACONDICIONADO">Reacondicionado</option>
            </select>
          </Fila>
        </div>
        <Fila etiqueta="Resumen (una línea, para la tarjeta del catálogo)" id="resumen">
          <input id="resumen" maxLength={300} value={ficha.resumen} onChange={(e) => cambiar("resumen", e.target.value)} className={ESTILO_DE_CAMPO} />
        </Fila>
        <Fila etiqueta="Descripción (dejá una línea en blanco entre párrafos)" id="descripcion">
          <textarea id="descripcion" rows={5} maxLength={5000} value={ficha.descripcion} onChange={(e) => cambiar("descripcion", e.target.value)} className={ESTILO_DE_CAMPO} />
        </Fila>
        <Fila etiqueta="Características (una por línea)" id="caracteristicas">
          <textarea
            id="caracteristicas"
            rows={4}
            value={caracteristicas}
            onChange={(e) => setCaracteristicas(e.target.value)}
            placeholder={"Full HD 1080p\nVisión nocturna\nApp en el celular"}
            className={ESTILO_DE_CAMPO}
          />
        </Fila>
        <div className="grid gap-4 sm:grid-cols-2">
          <Fila etiqueta="Garantía (meses, opcional)" id="garantia">
            <input
              id="garantia"
              type="number"
              min={1}
              max={120}
              value={ficha.garantiaMeses ?? ""}
              onChange={(e) => cambiar("garantiaMeses", e.target.value ? Number(e.target.value) : null)}
              className={ESTILO_DE_CAMPO}
            />
          </Fila>
          {!producto && (
            <Fila etiqueta="Precio (vacío = a consultar)" id="precio">
              <input id="precio" inputMode="numeric" value={precio} onChange={(e) => setPrecio(e.target.value)} placeholder="45000" className={`numeros ${ESTILO_DE_CAMPO}`} />
            </Fila>
          )}
        </div>
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={ficha.publicado} onChange={(e) => cambiar("publicado", e.target.checked)} />
          Se ve en la tienda como producto
        </label>

        <DatosTecnicos tecnicos={ficha.tecnicos} alCambiar={(t) => cambiar("tecnicos", t)} />
      </fieldset>

      {!soloLectura && (
        <div className="mt-6 flex justify-end">
          <button type="submit" disabled={enviando} className="boton-oro rounded-full px-6 py-2.5 font-semibold hover:brightness-110 disabled:opacity-50">
            {enviando ? "Guardando…" : producto ? "Guardar cambios" : "Crear producto"}
          </button>
        </div>
      )}
    </form>
  );
}

const TIPOS: TipoComponente[] = ["CPU", "MOTHER", "RAM", "GPU", "ALMACENAMIENTO", "FUENTE", "GABINETE"];

/** Los datos que usa la compatibilidad de Armá tu PC: cada tipo pide solo los suyos. */
function DatosTecnicos({ tecnicos, alCambiar }: { tecnicos: Tecnicos | null; alCambiar: (t: Tecnicos | null) => void }) {
  const t = tecnicos;
  const cambiar = (cambios: Partial<Tecnicos>) => t && alCambiar({ ...t, ...cambios });
  const numero = (valor: string) => (valor ? Number(valor) : null);

  return (
    <div className="rounded-lg border border-borde p-4">
      <label className="flex items-center gap-2 font-semibold">
        <input
          type="checkbox"
          checked={t !== null}
          onChange={(e) => alCambiar(e.target.checked ? { tipo: "CPU", formatosSoportados: [] } : null)}
        />
        Es un componente de Armá tu PC
      </label>
      {t && (
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <Fila etiqueta="Tipo de componente" id="tipo">
            <select id="tipo" value={t.tipo} onChange={(e) => alCambiar({ tipo: e.target.value as TipoComponente, formatosSoportados: [] })} className={ESTILO_DE_CAMPO}>
              {TIPOS.map((tipo) => (
                <option key={tipo} value={tipo}>
                  {NOMBRE_DEL_TIPO[tipo]}
                </option>
              ))}
            </select>
          </Fila>
          {(t.tipo === "CPU" || t.tipo === "MOTHER") && (
            <Fila etiqueta="Socket" id="socket">
              <input id="socket" required value={t.socket ?? ""} onChange={(e) => cambiar({ socket: e.target.value })} placeholder="AM4, AM5, LGA1700…" className={ESTILO_DE_CAMPO} />
            </Fila>
          )}
          {(t.tipo === "MOTHER" || t.tipo === "RAM") && (
            <Fila etiqueta="Tipo de memoria" id="tipoRam">
              <select id="tipoRam" required value={t.tipoRam ?? ""} onChange={(e) => cambiar({ tipoRam: e.target.value as TipoRam })} className={ESTILO_DE_CAMPO}>
                <option value="" disabled>
                  Elegí…
                </option>
                <option value="DDR4">DDR4</option>
                <option value="DDR5">DDR5</option>
              </select>
            </Fila>
          )}
          {t.tipo === "MOTHER" && (
            <Fila etiqueta="Formato" id="formato">
              <select id="formato" required value={t.formato ?? ""} onChange={(e) => cambiar({ formato: e.target.value as FormatoMother })} className={ESTILO_DE_CAMPO}>
                <option value="" disabled>
                  Elegí…
                </option>
                <option value="ATX">ATX</option>
                <option value="MATX">Micro ATX</option>
              </select>
            </Fila>
          )}
          {(t.tipo === "CPU" || t.tipo === "GPU") && (
            <Fila etiqueta="Consumo (W)" id="consumo">
              <input id="consumo" type="number" min={1} max={3000} required value={t.consumoW ?? ""} onChange={(e) => cambiar({ consumoW: numero(e.target.value) })} className={ESTILO_DE_CAMPO} />
            </Fila>
          )}
          {t.tipo === "FUENTE" && (
            <Fila etiqueta="Potencia (W)" id="potencia">
              <input id="potencia" type="number" min={1} max={3000} required value={t.potenciaW ?? ""} onChange={(e) => cambiar({ potenciaW: numero(e.target.value) })} className={ESTILO_DE_CAMPO} />
            </Fila>
          )}
          {t.tipo === "CPU" && (
            <label className="flex items-center gap-2 self-end pb-3">
              <input type="checkbox" checked={!!t.graficosIntegrados} onChange={(e) => cambiar({ graficosIntegrados: e.target.checked })} />
              Tiene gráficos integrados
            </label>
          )}
          {t.tipo === "GABINETE" && (
            <fieldset className="sm:col-span-2">
              <legend className="text-sm text-suave">Mothers que entran</legend>
              <div className="mt-1 flex gap-4">
                {(["ATX", "MATX"] as FormatoMother[]).map((f) => (
                  <label key={f} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={!!t.formatosSoportados?.includes(f)}
                      onChange={(e) =>
                        cambiar({
                          formatosSoportados: e.target.checked
                            ? [...(t.formatosSoportados ?? []), f]
                            : (t.formatosSoportados ?? []).filter((x) => x !== f),
                        })
                      }
                    />
                    {f === "MATX" ? "Micro ATX" : "ATX"}
                  </label>
                ))}
              </div>
            </fieldset>
          )}
        </div>
      )}
    </div>
  );
}

function PrecioDelProducto({ producto, soloLectura }: { producto: DetalleDeProducto; soloLectura: boolean }) {
  const cambiar = useCambiarPrecio(producto.id);
  const avisar = useAvisar();
  const [texto, setTexto] = useState(producto.precio === null ? "" : String(producto.precio));

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    const nuevo = texto.trim() ? leerPrecio(texto) : null;
    if (texto.trim() && nuevo === null) return avisar("El precio tiene que ser un número de pesos, sin centavos", "error");
    cambiar.mutate(nuevo, { onSuccess: () => avisar("Precio actualizado"), onError: (e) => avisar(e.message, "error") });
  }

  return (
    <form onSubmit={guardar} className="rounded-xl border border-borde bg-superficie p-5">
      <h2 className="text-2xl">Precio de lista</h2>
      <p className="mt-1 text-sm text-suave">
        Ahora: <span className="numeros text-principal">{formatearPrecio(producto.precio)}</span>. Cada cambio queda en el historial (Precios).
      </p>
      <fieldset disabled={soloLectura} className="mt-3 flex gap-2">
        <label htmlFor="precio-lista" className="sr-only">
          Precio de lista
        </label>
        <input id="precio-lista" inputMode="numeric" value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="A consultar" className={`numeros ${ESTILO_DE_CAMPO}`} />
        <button type="submit" disabled={cambiar.isPending} className="shrink-0 rounded-full border border-borde px-4 text-sm font-semibold hover:border-oro">
          Guardar
        </button>
      </fieldset>
    </form>
  );
}

function Fila({ etiqueta, id, children }: { etiqueta: string; id: string; children: ReactNode }) {
  return (
    <div>
      <label htmlFor={id} className="text-sm text-suave">
        {etiqueta}
      </label>
      <div className="mt-1">{children}</div>
    </div>
  );
}
