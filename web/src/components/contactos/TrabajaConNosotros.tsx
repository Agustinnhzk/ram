"use client";

import { useState, type FormEvent } from "react";
import { AreaDeTexto, Aviso, BotonPrincipal, Campo, CampoTrampa } from "@/components/formularios";
import { IconoCheck, IconoHerramienta, IconoMonitoreo } from "@/components/Iconos";
import { AREAS, consultarComoEmpresa, MOTIVOS, postularse, type MotivoDeEmpresa } from "@/lib/contactos";

type Opcion = "persona" | "empresa";
const TAMANO_MAXIMO = 5 * 1024 * 1024;

/** Dos caminos: quien quiere trabajar en RAM, y la empresa que quiere tercerizar su IT o asociarse. */
export function TrabajaConNosotros() {
  const [opcion, setOpcion] = useState<Opcion | null>(null);

  return (
    <>
      <div className="mt-10 grid gap-4 sm:grid-cols-2" role="tablist" aria-label="¿Quién sos?">
        <Eleccion
          activa={opcion === "persona"}
          onClick={() => setOpcion("persona")}
          icono={<IconoHerramienta className="size-8 text-oro" />}
          titulo="Quiero trabajar en RAM"
          detalle="Dejanos tus datos y tu CV. Buscamos técnicos, instaladores y gente de ventas."
        />
        <Eleccion
          activa={opcion === "empresa"}
          onClick={() => setOpcion("empresa")}
          icono={<IconoMonitoreo className="size-8 text-oro" />}
          titulo="Soy una empresa"
          detalle="Tercerizá el soporte IT de tu empresa o proponenos una alianza comercial."
        />
      </div>

      {opcion === "persona" && <FormularioPostulacion />}
      {opcion === "empresa" && <FormularioEmpresa />}
    </>
  );
}

function Eleccion(props: { activa: boolean; onClick: () => void; icono: React.ReactNode; titulo: string; detalle: string }) {
  return (
    <button
      type="button"
      role="tab"
      aria-selected={props.activa}
      onClick={props.onClick}
      className={`rounded-2xl border p-6 text-left transition-colors ${
        props.activa ? "border-oro bg-oro/10" : "border-borde bg-superficie hover:border-oro/60"
      }`}
    >
      {props.icono}
      <span className="mt-3 block font-display text-2xl font-bold text-principal italic">{props.titulo}</span>
      <span className="mt-1 block text-suave">{props.detalle}</span>
    </button>
  );
}

function Enviado({ texto }: { texto: string }) {
  return (
    <div className="mt-8 max-w-2xl">
      <Aviso tipo="exito">
        <span className="inline-flex items-center gap-2 font-semibold">
          <IconoCheck className="size-4 text-oro" /> ¡Listo, lo recibimos!
        </span>{" "}
        {texto}
      </Aviso>
    </div>
  );
}

function FormularioPostulacion() {
  const [cv, setCv] = useState<File | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [enviado, setEnviado] = useState(false);

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!cv) return setError("Adjuntá tu CV.");
    setEnviando(true);
    setError(null);
    // FormData toma todos los campos del formulario (con su "name"), incluido el archivo y el campo trampa.
    const resultado = await postularse(new FormData(evento.currentTarget));
    setEnviando(false);
    if (resultado.ok) setEnviado(true);
    else setError(resultado.mensaje);
  }

  if (enviado) return <Enviado texto="Leemos todas las postulaciones. Si tu perfil encaja con lo que buscamos, te escribimos." />;

  return (
    <form onSubmit={enviar} className="relative mt-8 max-w-2xl space-y-4 rounded-2xl border border-borde bg-superficie p-6 sm:p-8">
      <h2 className="text-3xl text-principal">Tu postulación</h2>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <CampoTrampa />
      <Campo etiqueta="Nombre y apellido" name="nombre" autoComplete="name" required maxLength={120} />
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo etiqueta="Correo" name="correo" type="email" autoComplete="email" required />
        <Campo etiqueta="Teléfono / WhatsApp" name="telefono" type="tel" autoComplete="tel" required maxLength={30} />
      </div>
      <div>
        <label htmlFor="area" className="text-sm text-suave">
          ¿En qué te gustaría trabajar?
        </label>
        <select
          id="area"
          name="area"
          className="mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal focus:border-oro focus:outline-none"
        >
          {AREAS.map((a) => (
            <option key={a}>{a}</option>
          ))}
        </select>
      </div>
      <AreaDeTexto etiqueta="Contanos de vos (opcional)" name="mensaje" maxLength={2000} placeholder="Experiencia, cursos, disponibilidad…" />
      <div>
        <p className="text-sm text-suave">CV</p>
        <div className="mt-1 flex flex-wrap items-center gap-3">
          <input
            id="cv"
            name="cv"
            type="file"
            accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            className="sr-only"
            onChange={(e) => {
              const elegido = e.target.files?.[0] ?? null;
              setError(null);
              if (elegido && elegido.size > TAMANO_MAXIMO) {
                e.target.value = "";
                setCv(null);
                return setError("El CV no puede superar los 5 MB.");
              }
              setCv(elegido);
            }}
          />
          <label htmlFor="cv" className="cursor-pointer rounded-full border border-borde px-5 py-2.5 text-sm text-principal hover:border-oro">
            {cv ? "Cambiar archivo" : "Adjuntar CV"}
          </label>
          <span className="max-w-full truncate text-sm text-principal">{cv?.name ?? "PDF o Word, hasta 5 MB"}</span>
        </div>
      </div>
      <BotonPrincipal enviando={enviando}>Enviar postulación</BotonPrincipal>
    </form>
  );
}

function FormularioEmpresa() {
  const [motivo, setMotivo] = useState<MotivoDeEmpresa>("TERCERIZAR_IT");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [enviado, setEnviado] = useState(false);

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    const datos = Object.fromEntries(new FormData(evento.currentTarget)) as Record<string, string>;
    const resultado = await consultarComoEmpresa({ ...datos, motivo });
    setEnviando(false);
    if (resultado.ok) setEnviado(true);
    else setError(resultado.mensaje);
  }

  if (enviado) return <Enviado texto="Te respondemos por mail o WhatsApp en las próximas 48 horas hábiles." />;

  return (
    <form onSubmit={enviar} className="relative mt-8 max-w-2xl space-y-5 rounded-2xl border border-borde bg-superficie p-6 sm:p-8">
      <h2 className="text-3xl text-principal">Contanos de tu empresa</h2>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <CampoTrampa />
      <fieldset>
        <legend className="text-sm text-suave">¿Qué estás buscando?</legend>
        <div className="mt-2 grid gap-3">
          {MOTIVOS.map((m) => (
            <label
              key={m.valor}
              className={`flex cursor-pointer gap-3 rounded-xl border p-4 ${motivo === m.valor ? "border-oro bg-oro/10" : "border-borde bg-fondo hover:border-oro/60"}`}
            >
              <input
                type="radio"
                name="motivo-elegido"
                checked={motivo === m.valor}
                onChange={() => setMotivo(m.valor)}
                className="mt-1 accent-[var(--oro)]"
              />
              <span>
                <span className="block font-semibold text-principal">{m.titulo}</span>
                <span className="block text-sm text-suave">{m.detalle}</span>
              </span>
            </label>
          ))}
        </div>
      </fieldset>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo etiqueta="Empresa" name="empresa" autoComplete="organization" required maxLength={150} />
        <Campo etiqueta="Tu nombre" name="contacto" autoComplete="name" required maxLength={120} />
        <Campo etiqueta="Correo" name="correo" type="email" autoComplete="email" required />
        <Campo etiqueta="Teléfono / WhatsApp" name="telefono" type="tel" autoComplete="tel" required maxLength={30} />
      </div>
      <AreaDeTexto
        etiqueta="Mensaje"
        name="mensaje"
        required
        maxLength={2000}
        placeholder="Cuántos equipos tienen, qué necesitan, qué nos proponés…"
      />
      <BotonPrincipal enviando={enviando}>Enviar</BotonPrincipal>
    </form>
  );
}
