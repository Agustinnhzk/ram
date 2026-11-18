import { useState, type FormEvent, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { useSesion } from "../../api/sesion";
import {
  NOMBRE_DEL_TIPO_DE_TRABAJO,
  useBarrios,
  useBorrarTrabajo,
  useCrearTrabajo,
  useEditarTrabajo,
  useTrabajo,
  type DatosDeTrabajo,
  type TipoDeTrabajo,
  type Trabajo,
} from "../../api/trabajos";
import { BotonBorrar } from "../../componentes/BotonBorrar";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { URL_TIENDA } from "../../utilidades/reparaciones";
import { FotosDelTrabajo } from "./FotosDelTrabajo";

const TIPOS = Object.keys(NOMBRE_DEL_TIPO_DE_TRABAJO) as TipoDeTrabajo[];

/** Hoy en la fecha de quien carga ("2026-11-17"), para el campo de fecha. */
function hoy(): string {
  const ahora = new Date();
  return new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60_000).toISOString().slice(0, 10);
}

function vacio(): DatosDeTrabajo {
  return {
    tipo: "NOTEBOOK",
    titulo: "",
    barrio: "",
    aDomicilio: false,
    problema: "",
    diagnostico: null,
    solucion: "",
    fecha: hoy(),
    numeroDeOrden: null,
    consentimiento: false,
    publicado: false,
  };
}

/** /trabajos/nuevo y /trabajos/:id — el caso; al editar, además, las fotos. */
export function Formulario() {
  const { id } = useParams();
  const { data: trabajo, isPending, isError, error } = useTrabajo(id);

  if (id && isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (id && isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar el trabajo: {error.message}
      </p>
    );
  }
  // key: si cambia de trabajo, el formulario arranca de cero con los datos nuevos.
  return <Contenido key={trabajo?.id ?? "nuevo"} trabajo={trabajo ?? null} />;
}

function Contenido({ trabajo }: { trabajo: Trabajo | null }) {
  const { data: sesion } = useSesion();
  const esDueno = sesion?.rol === "DUENO";
  const borrar = useBorrarTrabajo(trabajo?.id ?? "");
  const navegar = useNavigate();

  return (
    <>
      <Link to="/trabajos" className="text-sm text-suave hover:text-oro-claro">
        ← Trabajos
      </Link>
      <div className="mt-2 flex flex-wrap items-end justify-between gap-3">
        <h1 className="texto-metalico text-4xl sm:text-5xl">{trabajo ? trabajo.titulo : "Nuevo trabajo"}</h1>
        {trabajo?.publicado && (
          <a href={`${URL_TIENDA}/trabajos/${trabajo.slug}`} target="_blank" rel="noreferrer" className="text-sm text-oro underline underline-offset-4">
            Ver en la tienda ↗
          </a>
        )}
      </div>
      {!esDueno && <p className="mt-2 text-suave">Solo el dueño puede cargar y publicar trabajos: acá podés verlos.</p>}

      <div className="mt-8 grid items-start gap-6 lg:grid-cols-[1fr_22rem]">
        <DatosDelTrabajo trabajo={trabajo} soloLectura={!esDueno} />
        <div className="space-y-6">
          <Privacidad />
          {trabajo && <FotosDelTrabajo trabajo={trabajo} soloLectura={!esDueno} />}
          {trabajo && esDueno && (
            <BotonBorrar
              texto="Borrar trabajo"
              titulo="¿Borrar este trabajo?"
              confirmar="Sí, borrar el trabajo y sus fotos"
              alBorrar={() => borrar.mutateAsync()}
              alTerminar={() => navegar("/trabajos", { replace: true })}
            >
              Se borra «{trabajo.titulo}» con todas sus fotos, y deja de verse en la tienda. Si solo querés sacarlo de la tienda,
              desmarcá «Publicado».
            </BotonBorrar>
          )}
        </div>
      </div>
    </>
  );
}

/** Lo que conviene recordar antes de publicar, sobre todo en cámaras y alarmas. */
function Privacidad() {
  return (
    <section className="rounded-xl border border-oro/50 bg-oro/5 p-5 text-sm">
      <h2 className="text-xl">Antes de publicar</h2>
      <ul className="mt-2 list-disc space-y-1.5 pl-5 text-suave">
        <li>Pedile permiso al cliente (aunque sea por WhatsApp).</li>
        <li>Solo el barrio o la localidad: nunca calle ni altura.</li>
        <li>Nada de fotos del frente, patentes, carteles o vistas que permitan reconocer la casa.</li>
        <li>En cámaras y alarmas, no muestres dónde quedó cada cámara ni el panel: mostrá el equipo, el cableado prolijo o la app.</li>
        <li>En equipos, que no se vean datos en la pantalla ni etiquetas con nombres.</li>
      </ul>
      <p className="mt-2 text-xs text-suave">La ubicación GPS que guarda el celular en las fotos se borra sola al subirlas.</p>
    </section>
  );
}

function DatosDelTrabajo({ trabajo, soloLectura }: { trabajo: Trabajo | null; soloLectura: boolean }) {
  const crear = useCrearTrabajo();
  const editar = useEditarTrabajo(trabajo?.id ?? "");
  const { data: barrios } = useBarrios();
  const avisar = useAvisar();
  const navegar = useNavigate();
  const [datos, setDatos] = useState<DatosDeTrabajo>(trabajo ?? vacio());
  const enviando = crear.isPending || editar.isPending;

  const cambiar = <K extends keyof DatosDeTrabajo>(campo: K, valor: DatosDeTrabajo[K]) => setDatos((d) => ({ ...d, [campo]: valor }));

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    const limpio: DatosDeTrabajo = { ...datos, diagnostico: datos.diagnostico?.trim() ? datos.diagnostico : null };
    if (trabajo) {
      editar.mutate(limpio, { onSuccess: () => avisar("Trabajo guardado"), onError: (e) => avisar(e.message, "error") });
      return;
    }
    crear.mutate(limpio, {
      onSuccess: ({ id }) => {
        avisar("Trabajo creado. Ahora podés agregarle fotos.");
        navegar(`/trabajos/${id}`, { replace: true });
      },
      onError: (e) => avisar(e.message, "error"),
    });
  }

  return (
    <form onSubmit={guardar} className="rounded-xl border border-borde bg-superficie p-5 sm:p-6">
      <fieldset disabled={soloLectura} className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo id="tipo" etiqueta="Tipo de trabajo">
            <select id="tipo" value={datos.tipo} onChange={(e) => cambiar("tipo", e.target.value as TipoDeTrabajo)} className={ESTILO_DE_CAMPO}>
              {TIPOS.map((t) => (
                <option key={t} value={t}>
                  {NOMBRE_DEL_TIPO_DE_TRABAJO[t]}
                </option>
              ))}
            </select>
          </Campo>
          <Campo id="fecha" etiqueta="Fecha del trabajo">
            <input id="fecha" type="date" required max={hoy()} value={datos.fecha} onChange={(e) => cambiar("fecha", e.target.value)} className={ESTILO_DE_CAMPO} />
          </Campo>
        </div>

        <Campo id="titulo" etiqueta="Título" ayuda="Lo que le pasaba al equipo o qué se instaló. Es el título de la página en Google.">
          <input
            id="titulo"
            required
            minLength={5}
            maxLength={120}
            value={datos.titulo}
            onChange={(e) => cambiar("titulo", e.target.value)}
            placeholder="Notebook que no encendía después de una tormenta"
            className={ESTILO_DE_CAMPO}
          />
        </Campo>

        <div className="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-end">
          <Campo
            id="barrio"
            etiqueta="Barrio o localidad"
            ayuda="Solo el barrio (Abasto, Alberdi) o la localidad (Funes). Nunca calle ni altura."
          >
            <input
              id="barrio"
              required
              maxLength={60}
              list="barrios-usados"
              autoComplete="off"
              value={datos.barrio}
              onChange={(e) => cambiar("barrio", e.target.value)}
              placeholder="Abasto"
              className={ESTILO_DE_CAMPO}
            />
            {/* Los barrios que ya usaste: al escribir "Alb" te ofrece "Alberdi", así no queda una zona repetida. */}
            <datalist id="barrios-usados">
              {barrios?.map((b) => (
                <option key={b} value={b} />
              ))}
            </datalist>
          </Campo>
          <label className="flex items-center gap-2 pb-8 sm:pb-9">
            <input type="checkbox" checked={datos.aDomicilio} onChange={(e) => cambiar("aDomicilio", e.target.checked)} />
            Fue a domicilio
          </label>
        </div>

        <Texto id="problema" etiqueta="El problema" valor={datos.problema} alCambiar={(v) => cambiar("problema", v)} requerido
          ejemplo="Se apagó durante una tormenta y no volvió a prender." />
        <Texto id="diagnostico" etiqueta="El diagnóstico" opcional valor={datos.diagnostico ?? ""} alCambiar={(v) => cambiar("diagnostico", v)}
          ejemplo="Falla en el circuito de carga de la placa." />
        <Texto id="solucion" etiqueta="Qué hicimos" valor={datos.solucion} alCambiar={(v) => cambiar("solucion", v)} requerido
          ejemplo="Reparación de placa, cambio de un componente y prueba de 24 h. Retiro y entrega a domicilio en 3 días." />

        <Campo id="orden" etiqueta="N° de orden" opcional ayuda="Solo para vos, para encontrar la reparación o instalación. No se publica.">
          <input
            id="orden"
            type="number"
            min={1}
            value={datos.numeroDeOrden ?? ""}
            onChange={(e) => cambiar("numeroDeOrden", e.target.value ? Number(e.target.value) : null)}
            className={`numeros max-w-40 ${ESTILO_DE_CAMPO}`}
          />
        </Campo>

        <div className="space-y-2 rounded-lg border border-borde p-4">
          <label className="flex items-start gap-2">
            <input
              type="checkbox"
              className="mt-1"
              checked={datos.consentimiento}
              onChange={(e) => setDatos((d) => ({ ...d, consentimiento: e.target.checked, publicado: e.target.checked && d.publicado }))}
            />
            <span>
              El cliente dio permiso para publicarlo
              <span className="block text-xs text-suave">Sin esto no se puede publicar.</span>
            </span>
          </label>
          <label className={`flex items-start gap-2 ${datos.consentimiento ? "" : "opacity-50"}`}>
            <input
              type="checkbox"
              className="mt-1"
              disabled={!datos.consentimiento}
              checked={datos.publicado}
              onChange={(e) => cambiar("publicado", e.target.checked)}
            />
            <span>
              Publicado
              <span className="block text-xs text-suave">Se ve en la tienda, en Trabajos realizados.</span>
            </span>
          </label>
        </div>
      </fieldset>

      {!soloLectura && (
        <div className="mt-6 flex justify-end">
          <button type="submit" disabled={enviando} className="boton-oro rounded-full px-6 py-2.5 font-semibold hover:brightness-110 disabled:opacity-50">
            {enviando ? "Guardando…" : trabajo ? "Guardar cambios" : "Crear trabajo"}
          </button>
        </div>
      )}
    </form>
  );
}

function Texto(props: {
  id: string;
  etiqueta: string;
  valor: string;
  alCambiar: (valor: string) => void;
  ejemplo: string;
  requerido?: boolean;
  opcional?: boolean;
}): ReactNode {
  return (
    <Campo id={props.id} etiqueta={props.etiqueta} opcional={props.opcional}>
      <textarea
        id={props.id}
        rows={3}
        required={props.requerido}
        minLength={props.requerido ? 3 : undefined}
        maxLength={1000}
        value={props.valor}
        onChange={(e) => props.alCambiar(e.target.value)}
        placeholder={props.ejemplo}
        className={ESTILO_DE_CAMPO}
      />
    </Campo>
  );
}
